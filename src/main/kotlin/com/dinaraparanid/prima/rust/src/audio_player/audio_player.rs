extern crate atomic_float;
extern crate futures;
extern crate futures_timer;
extern crate once_cell;
extern crate rodio;
extern crate tokio;

use atomic_float::AtomicF32;
use futures::future::{AbortHandle, Abortable};
use futures_timer::Delay;
use once_cell::sync::Lazy;
use rodio::{source::Buffered, Decoder, OutputStream, OutputStreamHandle, Sink, Source};

use std::{
    fs::File,
    io::BufReader,
    path::PathBuf,
    sync::{
        atomic::{AtomicBool, Ordering},
        Arc,
    },
    time::Duration,
};

use tokio::{sync::RwLock, task::JoinHandle};

use crate::{
    audio_player::{
        playback_params::*, playback_position_controller::PlaybackPositionController, result::*,
    },
    get_cur_playlist_async, PlaylistTrait, StorageUtil, TrackTrait, PARAMS,
};

pub struct AudioPlayer {
    source_path: Option<PathBuf>,
    playback_data: Option<(OutputStream, OutputStreamHandle, Sink)>,
    total_duration: Duration,
    is_playing: Arc<AtomicBool>,
    playback_params: PlaybackParams,
    playback_position_controller: Arc<RwLock<PlaybackPositionController>>,
}

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
            total_duration: Duration::default(),
        }
    }

    #[inline]
    async fn run_playback_control_task(
        is_playing: Arc<AtomicBool>,
        playback_position_controller: Arc<RwLock<PlaybackPositionController>>,
        speed: Arc<AtomicF32>,
        max_duration: Duration,
    ) {
        let (handle, reg) = AbortHandle::new_pair();
        let is_playing_clone = is_playing.clone();
        let position_clone = playback_position_controller.read().await.position.clone();

        let task = Abortable::new(
            async move {
                while is_playing_clone.load(Ordering::SeqCst) {
                    Delay::new(Duration::from_millis((50.0) as u64)).await;

                    if is_playing_clone.load(Ordering::SeqCst) {
                        let cur_dur = *position_clone.read().await
                            + Duration::from_millis((50.0 * speed.load(Ordering::SeqCst)) as u64);

                        if cur_dur > max_duration {
                            *position_clone.write().await = max_duration;
                            is_playing_clone.store(false, Ordering::SeqCst);
                            break;
                        }

                        *position_clone.write().await = cur_dur;
                    }
                }
            },
            reg,
        );

        playback_position_controller.write().await.task = Some(handle);
        task.await.unwrap_or_default()
    }

    #[inline]
    async fn get_buffered_source(&mut self) -> Result<Buffered<Decoder<BufReader<File>>>> {
        if self.source_path.is_none() {
            self.source_path = Some(unsafe {
                get_cur_playlist_async!()
                    .get_cur_track()
                    .unwrap()
                    .get_path()
                    .clone()
            });
        }

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
    async fn run_playback_preparation_tasks(
        is_playing: Arc<AtomicBool>,
        playback_position_controller: Arc<RwLock<PlaybackPositionController>>,
        speed: Arc<AtomicF32>,
        max_duration: Duration,
    ) {
        unsafe {
            PARAMS.read().await.as_ref().unwrap().tokio_runtime.spawn(
                AudioPlayer::run_playback_control_task(
                    is_playing,
                    playback_position_controller,
                    speed,
                    max_duration,
                ),
            );
        }
    }

    #[inline]
    async fn abort_playback_position_controller_tasks(&self) {
        self.playback_position_controller
            .write()
            .await
            .task
            .as_ref()
            .map(|t| t.abort())
            .unwrap_or_default();
    }

    #[inline]
    async fn play_with_result(
        this: Arc<RwLock<AudioPlayer>>,
        source: PathBuf,
        track_duration: Duration,
    ) -> Result<()> {
        this.read()
            .await
            .abort_playback_position_controller_tasks()
            .await;

        this.write().await.source_path = Some(source);
        this.write().await.total_duration = track_duration;

        *this
            .write()
            .await
            .playback_position_controller
            .write()
            .await
            .position
            .write()
            .await = Duration::default();

        this.read().await.save_cur_playback_pos_async().await;

        let src = Source::buffered(Source::fade_in(
            Source::reverb(
                this.write().await.get_buffered_source().await?,
                this.read()
                    .await
                    .playback_params
                    .get_reverb()
                    .get_duration(),
                this.read()
                    .await
                    .playback_params
                    .get_reverb()
                    .get_amplitude(),
            ),
            this.read().await.playback_params.get_fade_in(),
        ));

        let (stream, handle) = OutputStream::try_default().unwrap();
        let sink = Sink::try_new(&handle).unwrap();
        this.write().await.playback_data = Some((stream, handle, sink));

        {
            let speed = this.read().await.playback_params.get_speed();
            let volume = this.read().await.playback_params.get_volume();
            let mut refer = this.write().await;
            let refer = &mut refer.playback_data.as_mut().unwrap().2;
            refer.append(src);
            refer.set_speed(speed);
            refer.set_volume(volume);
        }

        this.read().await.is_playing.store(true, Ordering::SeqCst);

        AudioPlayer::run_playback_preparation_tasks(
            this.read().await.is_playing.clone(),
            this.read().await.playback_position_controller.clone(),
            this.read().await.get_speed_ref(),
            this.read().await.total_duration,
        )
        .await;

        Ok(())
    }

    #[inline]
    pub async fn play(source: PathBuf, track_duration: Duration) {
        AudioPlayer::play_with_result(unsafe { AUDIO_PLAYER.clone() }, source, track_duration)
            .await
            .unwrap_or_default()
    }

    #[inline]
    pub async fn save_cur_playback_pos_async(&self) -> JoinHandle<()> {
        let pos = self.get_cur_playback_pos().await.as_millis() as u64;

        unsafe {
            PARAMS
                .read()
                .await
                .as_ref()
                .unwrap()
                .tokio_runtime
                .spawn(async move {
                    StorageUtil::store_current_playback_position(pos).unwrap_or_default()
                })
        }
    }

    #[inline]
    pub async fn pause(&mut self) {
        self.is_playing.store(false, Ordering::SeqCst);

        if let Some(task) = &self.playback_position_controller.read().await.task {
            task.abort()
        }

        self.playback_data.as_mut().unwrap().2.stop();
        self.save_cur_playback_pos_async().await;
    }

    #[inline]
    async fn resume_with_result(&mut self, track_duration: Duration) -> Result<()> {
        let cur_duration = self.get_cur_playback_pos().await;
        self.seek_to_with_result(cur_duration, track_duration).await
    }

    #[inline]
    pub async fn resume(&mut self, track_duration: Duration) {
        self.resume_with_result(track_duration)
            .await
            .unwrap_or_default()
    }

    #[inline]
    pub async fn stop(&mut self) {
        self.is_playing.store(false, Ordering::SeqCst);

        if let Some(task) = &self.playback_position_controller.read().await.task {
            task.abort()
        }

        self.playback_data
            .as_mut()
            .map(|pd| pd.2.stop())
            .unwrap_or_default();

        self.save_cur_playback_pos_async().await;
    }

    #[inline]
    async fn seek_to_with_result(
        &mut self,
        position: Duration,
        track_duration: Duration,
    ) -> Result<()> {
        let src = Source::buffered(Source::skip_duration(
            Source::fade_in(
                Source::reverb(
                    self.get_buffered_source().await?,
                    self.playback_params.get_reverb().get_duration(),
                    self.playback_params.get_reverb().get_amplitude(),
                ),
                self.playback_params.get_fade_in(),
            ),
            position,
        ));

        self.stop().await;

        let (stream, handle) = OutputStream::try_default().unwrap();
        let sink = Sink::try_new(&handle).unwrap();
        self.playback_data = Some((stream, handle, sink));
        self.total_duration = track_duration;

        {
            let speed = self.playback_params.get_speed();
            let volume = self.playback_params.get_volume();
            let refer = &mut self.playback_data.as_mut().unwrap().2;
            refer.append(src);
            refer.set_speed(speed);
            refer.set_volume(volume);
        }

        self.abort_playback_position_controller_tasks().await;
        self.is_playing.store(true, Ordering::SeqCst);

        *self
            .playback_position_controller
            .write()
            .await
            .position
            .write()
            .await = position;

        AudioPlayer::run_playback_preparation_tasks(
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed_ref(),
            self.total_duration,
        )
        .await;

        self.save_cur_playback_pos_async().await;
        Ok(())
    }

    #[inline]
    pub async fn seek_to(&mut self, position: Duration, track_duration: Duration) {
        self.seek_to_with_result(position, track_duration)
            .await
            .unwrap_or_default()
    }

    #[inline]
    pub fn get_volume(&self) -> f32 {
        self.playback_params.get_volume()
    }

    #[inline]
    pub fn get_speed(&self) -> f32 {
        self.playback_params.get_speed()
    }

    #[inline]
    pub fn get_speed_ref(&self) -> Arc<AtomicF32> {
        self.playback_params.get_speed_ref()
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

        if let Some(ref pd) = self.playback_data {
            pd.2.set_volume(volume)
        }
    }

    #[inline]
    pub fn set_speed(&mut self, speed: f32) {
        self.playback_params.set_speed(speed);
        let speed = self.playback_params.get_speed();

        if let Some(ref pd) = self.playback_data {
            pd.2.set_speed(speed)
        }
    }

    #[inline]
    pub async fn set_reverb(&mut self, reverb: ReverbParams) -> Result<()> {
        self.playback_params.set_reverb(reverb);
        self.abort_playback_position_controller_tasks().await;

        let pos = *self
            .playback_position_controller
            .read()
            .await
            .position
            .read()
            .await;

        let src = Source::buffered(Source::skip_duration(
            Source::reverb(
                self.get_buffered_source().await?,
                reverb.get_duration(),
                reverb.get_amplitude(),
            ),
            pos,
        ));

        self.playback_data.as_mut().unwrap().2.append(src);
        self.is_playing.store(true, Ordering::SeqCst);

        AudioPlayer::run_playback_preparation_tasks(
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed_ref(),
            self.total_duration,
        )
        .await;

        Ok(())
    }

    #[inline]
    pub async fn set_fade_in(&mut self, fade_in: Duration) -> Result<()> {
        self.playback_params.set_fade_in(fade_in);
        self.abort_playback_position_controller_tasks().await;

        let pos = *self
            .playback_position_controller
            .read()
            .await
            .position
            .read()
            .await;

        let src = Source::buffered(Source::skip_duration(
            Source::fade_in(self.get_buffered_source().await?, fade_in),
            pos,
        ));

        self.playback_data.as_mut().unwrap().2.append(src);
        self.is_playing.store(true, Ordering::SeqCst);

        AudioPlayer::run_playback_preparation_tasks(
            self.is_playing.clone(),
            self.playback_position_controller.clone(),
            self.get_speed_ref(),
            self.total_duration,
        )
        .await;

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
    pub async fn get_cur_playback_pos(&self) -> Duration {
        *self
            .playback_position_controller
            .read()
            .await
            .position
            .read()
            .await
    }

    #[inline]
    pub fn is_done(&self) -> bool {
        self.playback_data.as_ref().unwrap().2.empty()
    }
}
