extern crate futures;
extern crate futures_timer;
extern crate once_cell;
extern crate rodio;

use futures_timer::Delay;
use once_cell::sync::Lazy;
use rodio::{source::Buffered, Decoder, OutputStream, OutputStreamHandle, Sink, Source};

use futures::{
    executor::ThreadPool,
    future::{AbortHandle, Abortable},
};

use std::{
    fs::File,
    io::BufReader,
    path::PathBuf,
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc, RwLock,
    },
    time::Duration,
};

use crate::audio_player::{
    playback_params::*, playback_position_controller::PlaybackPositionController, result::*,
};

pub struct AudioPlayer {
    source_path: Option<PathBuf>,
    playback_data: Option<(OutputStream, OutputStreamHandle, Sink)>,
    total_duration: Duration,
    is_playing: Arc<AtomicBool>,
    playback_params: PlaybackParams,
    playback_position_controller: Arc<RwLock<PlaybackPositionController>>,
    pool: ThreadPool,
}

// TODO: Load playback params
pub static mut AUDIO_PLAYER: Lazy<Arc<RwLock<AudioPlayer>>> =
    Lazy::new(|| Arc::new(RwLock::new(AudioPlayer::new(PlaybackParams::default()))));

impl AudioPlayer {
    #[inline]
    pub fn new(playback_params: PlaybackParams) -> Self {
        AudioPlayer {
            source_path: None,
            playback_data: None,
            is_playing: Arc::new(AtomicBool::default()),
            playback_params,
            playback_position_controller: Arc::new(RwLock::new(
                PlaybackPositionController::default(),
            )),
            pool: ThreadPool::builder().pool_size(1).create().unwrap(),
            total_duration: Duration::default(),
        }
    }

    #[inline]
    async fn run_playback_control_task(
        is_playing: Arc<AtomicBool>,
        playback_position_controller: Arc<RwLock<PlaybackPositionController>>,
        speed: f32,
        max_duration: Duration,
    ) {
        let (handle, reg) = AbortHandle::new_pair();
        let is_playing_clone = is_playing.clone();

        let position_clone = playback_position_controller
            .read()
            .unwrap()
            .position
            .clone();

        let task = Abortable::new(
            async move {
                while is_playing_clone.load(Ordering::SeqCst) {
                    Delay::new(Duration::from_millis((50.0 * speed) as u64)).await;

                    if is_playing_clone.load(Ordering::SeqCst) {
                        let cur_dur = *position_clone.read().unwrap() + Duration::from_millis(50);

                        if cur_dur > max_duration {
                            *position_clone.write().unwrap() = max_duration;
                            is_playing_clone.store(false, Ordering::SeqCst);
                            break;
                        }

                        *position_clone.write().unwrap() = cur_dur;
                    }
                }
            },
            reg,
        );

        playback_position_controller.write().unwrap().task = Some(handle);
        task.await.unwrap_or(())
    }

    #[inline]
    fn get_buffered_source(&self) -> Result<Buffered<Decoder<BufReader<File>>>> {
        Ok(Source::buffered(
            match Decoder::new(BufReader::new(
                match File::open(self.source_path.as_ref().unwrap().clone()) {
                    Ok(x) => x,
                    Err(_) => return Err(Error::FileOpeningError),
                },
            )) {
                Ok(x) => x,
                Err(_) => return Err(Error::FileNotSupportedError),
            },
        ))
    }

    #[inline]
    fn run_playback_preparation_tasks(
        pool: ThreadPool,
        is_playing: Arc<AtomicBool>,
        playback_position_controller: Arc<RwLock<PlaybackPositionController>>,
        speed: f32,
        max_duration: Duration,
    ) {
        pool.spawn_ok(AudioPlayer::run_playback_control_task(
            is_playing,
            playback_position_controller,
            speed,
            max_duration,
        ));
    }

    #[inline]
    fn play_with_result(&mut self, source: PathBuf, track_duration: Duration) -> Result<()> {
        self.source_path = Some(source);
        self.total_duration = track_duration;

        *self
            .playback_position_controller
            .write()
            .unwrap()
            .position
            .write()
            .unwrap() = Duration::default();

        let src = Source::buffered(Source::fade_in(
            Source::reverb(
                self.get_buffered_source()?,
                self.playback_params.get_reverb().get_duration(),
                self.playback_params.get_reverb().get_amplitude(),
            ),
            self.playback_params.get_fade_in(),
        ));

        let (stream, handle) = OutputStream::try_default().unwrap();
        let sink = Sink::try_new(&handle).unwrap();
        self.playback_data = Some((stream, handle, sink));

        {
            let speed = self.playback_params.get_speed();
            let volume = self.playback_params.get_volume();
            let refer = &mut self.playback_data.as_mut().unwrap().2;
            refer.append(src);
            refer.set_speed(speed);
            refer.set_volume(volume);
        }

        self.is_playing.store(true, Ordering::SeqCst);

        AudioPlayer::run_playback_preparation_tasks(
            self.pool.clone(),
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed(),
            self.total_duration,
        );

        Ok(())
    }

    #[inline]
    pub async fn play(&mut self, source: PathBuf, track_duration: Duration) {
        self.play_with_result(source, track_duration).unwrap_or(())
    }

    #[inline]
    pub fn pause(&mut self) {
        self.is_playing.store(false, Ordering::SeqCst);

        if let Some(task) = &self.playback_position_controller.read().unwrap().task {
            task.abort()
        }

        self.playback_data.as_mut().unwrap().2.stop()
    }

    #[inline]
    fn resume_with_result(&mut self) -> Result<()> {
        let cur_duration = *self
            .playback_position_controller
            .read()
            .unwrap()
            .position
            .read()
            .unwrap();

        self.seek_to_with_result(cur_duration)
    }

    #[inline]
    pub fn resume(&mut self) {
        self.resume_with_result().unwrap_or(())
    }

    #[inline]
    pub fn stop(&mut self) {
        self.is_playing.store(false, Ordering::SeqCst);

        if let Some(task) = &self.playback_position_controller.read().unwrap().task {
            task.abort()
        }

        self.playback_data.as_mut().unwrap().2.stop();
    }

    #[inline]
    fn seek_to_with_result(&mut self, position: Duration) -> Result<()> {
        let src = Source::buffered(Source::skip_duration(
            Source::fade_in(
                Source::reverb(
                    self.get_buffered_source()?,
                    self.playback_params.get_reverb().get_duration(),
                    self.playback_params.get_reverb().get_amplitude(),
                ),
                self.playback_params.get_fade_in(),
            ),
            position,
        ));

        self.stop();

        let (stream, handle) = OutputStream::try_default().unwrap();
        let sink = Sink::try_new(&handle).unwrap();
        self.playback_data = Some((stream, handle, sink));

        {
            let speed = self.playback_params.get_speed();
            let volume = self.playback_params.get_volume();
            let refer = &mut self.playback_data.as_mut().unwrap().2;
            refer.append(src);
            refer.set_speed(speed);
            refer.set_volume(volume);
        }

        self.is_playing.store(true, Ordering::SeqCst);

        *self
            .playback_position_controller
            .write()
            .unwrap()
            .position
            .write()
            .unwrap() = position;

        AudioPlayer::run_playback_preparation_tasks(
            self.pool.clone(),
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed(),
            self.total_duration,
        );

        Ok(())
    }

    #[inline]
    pub fn seek_to(&mut self, position: Duration) {
        self.seek_to_with_result(position).unwrap_or(())
    }

    #[inline]
    pub fn get_volume(&self) -> f32 {
        self.playback_data.as_ref().unwrap().2.volume()
    }

    #[inline]
    pub fn get_speed(&self) -> f32 {
        self.playback_data.as_ref().unwrap().2.speed()
    }

    #[inline]
    pub fn get_reverb(&self) -> ReverbParams {
        self.playback_params.get_reverb()
    }

    #[inline]
    pub fn get_fade_in(&self) -> Duration {
        self.playback_params.get_fade_in()
    }

    #[inline]
    pub fn get_looping_state(&self) -> LoopingState {
        self.playback_params.get_looping_state()
    }

    #[inline]
    pub fn set_volume(&mut self, volume: f32) {
        self.playback_params.set_volume(volume);
        let volume = self.playback_params.get_volume();
        self.playback_data.as_mut().unwrap().2.set_volume(volume)
    }

    #[inline]
    pub fn set_speed(&mut self, speed: f32) {
        self.playback_params.set_speed(speed);
        let speed = self.playback_params.get_speed();
        self.playback_data.as_mut().unwrap().2.set_speed(speed)
    }

    #[inline]
    pub async fn set_reverb(&mut self, reverb: ReverbParams) -> Result<()> {
        self.playback_params.set_reverb(reverb);

        let pos = *self
            .playback_position_controller
            .read()
            .unwrap()
            .position
            .read()
            .unwrap();

        let src = Source::buffered(Source::skip_duration(
            Source::reverb(
                self.get_buffered_source()?,
                reverb.get_duration(),
                reverb.get_amplitude(),
            ),
            pos,
        ));

        self.playback_data.as_mut().unwrap().2.append(src);
        self.is_playing.store(true, Ordering::SeqCst);

        AudioPlayer::run_playback_preparation_tasks(
            self.pool.clone(),
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed(),
            self.total_duration,
        );

        Ok(())
    }

    #[inline]
    pub async fn set_fade_in(&mut self, fade_in: Duration) -> Result<()> {
        self.playback_params.set_fade_in(fade_in);

        let pos = *self
            .playback_position_controller
            .read()
            .unwrap()
            .position
            .read()
            .unwrap();

        let src = Source::buffered(Source::skip_duration(
            Source::fade_in(self.get_buffered_source()?, fade_in),
            pos,
        ));

        self.playback_data.as_mut().unwrap().2.append(src);
        self.is_playing.store(true, Ordering::SeqCst);

        AudioPlayer::run_playback_preparation_tasks(
            self.pool.clone(),
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed(),
            self.total_duration,
        );

        Ok(())
    }

    #[inline]
    pub fn set_next_looping_state(&mut self) {
        self.playback_params.set_next_looping_state()
    }

    #[inline]
    pub fn is_playing(&self) -> bool {
        self.is_playing.load(Ordering::SeqCst)
    }

    #[inline]
    pub fn get_cur_path(&self) -> Option<&PathBuf> {
        self.source_path.as_ref()
    }

    #[inline]
    pub fn get_cur_playback_pos(&self) -> Duration {
        *self
            .playback_position_controller
            .read()
            .unwrap()
            .position
            .read()
            .unwrap()
    }

    #[inline]
    pub fn is_done(&self) -> bool {
        self.playback_data.as_ref().unwrap().2.empty()
    }
}
