extern crate futures;
extern crate futures_timer;
extern crate rodio;

use futures::future::AbortHandle;
use futures_timer::Delay;
use rodio::{source::Buffered, Decoder, OutputStream, Sink, Source};

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

use crate::audio_player::{
    playback_params::*, playback_position_controller::PlaybackPositionController, result::*,
};

pub struct AudioPlayer {
    source_path: Option<PathBuf>,
    sink: Sink,
    is_playing: Arc<AtomicBool>,
    playback_params: PlaybackParams,
    playback_position_controller: PlaybackPositionController,
}

impl AudioPlayer {
    #[inline]
    pub fn new(playback_params: PlaybackParams) -> Self {
        AudioPlayer {
            source_path: None,
            sink: Sink::try_new(&(OutputStream::try_default().unwrap().1)).unwrap(),
            is_playing: Arc::new(AtomicBool::default()),
            playback_params,
            playback_position_controller: PlaybackPositionController::default(),
        }
    }

    #[inline]
    async fn run_playback_counter_task(&mut self) {
        let (handle, reg) = AbortHandle::new_pair();
        let is_playing_clone = self.is_playing.clone();
        let position_clone = self.playback_position_controller.position.clone();
        let speed = self.get_speed();

        let task = futures::future::Abortable::new(
            async move {
                while is_playing_clone.load(Ordering::SeqCst) {
                    Delay::new(Duration::from_secs_f32(1.0 * speed));

                    if is_playing_clone.load(Ordering::SeqCst) {
                        let cur_dur = *position_clone.read().unwrap() + Duration::from_secs(1);
                        *position_clone.write().unwrap() = cur_dur
                    }
                }
            },
            reg,
        );

        self.playback_position_controller.task = Some(handle);
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
    fn play(&mut self, source: PathBuf) -> Result<()> {
        let src = Source::buffered(Source::fade_in(
            Source::reverb(
                self.get_buffered_source()?,
                self.playback_params.get_reverb().get_duration(),
                self.playback_params.get_reverb().get_amplitude(),
            ),
            self.playback_params.get_fade_in(),
        ));

        self.source_path = Some(source);
        self.sink.set_speed(self.playback_params.get_speed());
        self.sink.set_volume(self.playback_params.get_volume());
        self.sink.append(src);
        self.is_playing.store(true, Ordering::SeqCst);
        Ok(())
    }

    #[inline]
    pub async fn play_and_with_counter_task(&mut self, source: PathBuf) {
        if self.play(source).is_ok() {
            self.run_playback_counter_task().await;
        }
    }

    #[inline]
    pub fn pause(&mut self) {
        self.is_playing.store(false, Ordering::SeqCst);

        if let Some(task) = &self.playback_position_controller.task {
            task.abort()
        }

        self.sink.pause()
    }

    #[inline]
    fn resume(&mut self) {
        self.sink.play();
        self.is_playing.store(true, Ordering::SeqCst);
    }

    #[inline]
    pub async fn resume_and_with_counter_task(&mut self) {
        self.resume();
        self.run_playback_counter_task().await
    }

    #[inline]
    pub fn stop(&mut self) {
        self.is_playing.store(false, Ordering::SeqCst);

        if let Some(task) = &self.playback_position_controller.task {
            task.abort()
        }

        self.sink.stop();
    }

    #[inline]
    fn seek_to(&mut self, position: Duration) -> Result<()> {
        let src = Source::buffered(Source::skip_duration(self.get_buffered_source()?, position));
        self.stop();
        self.sink.append(src);
        self.is_playing.store(true, Ordering::SeqCst);
        Ok(())
    }

    #[inline]
    pub async fn seek_to_and_with_counter_task(&mut self, position: Duration) {
        if self.seek_to(position).is_ok() {
            self.run_playback_counter_task().await
        }
    }

    #[inline]
    pub fn get_volume(&self) -> f32 {
        self.sink.volume()
    }

    #[inline]
    pub fn get_speed(&self) -> f32 {
        self.sink.speed()
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
        self.sink.set_volume(volume)
    }

    #[inline]
    pub fn set_speed(&mut self, speed: f32) {
        self.playback_params.set_speed(speed);
        let speed = self.playback_params.get_speed();
        self.sink.set_speed(speed)
    }

    #[inline]
    fn set_reverb(&mut self, reverb: ReverbParams) -> Result<()> {
        self.playback_params.set_reverb(reverb);

        let src = Source::buffered(Source::reverb(
            self.get_buffered_source()?,
            reverb.get_duration(),
            reverb.get_amplitude(),
        ));

        self.sink.stop();

        let pos = *self.playback_position_controller.position.read().unwrap();
        self.seek_to(pos)?;

        self.sink.append(src);
        Ok(())
    }

    #[inline]
    pub async fn set_reverb_with_counter_task(&mut self, reverb: ReverbParams) {
        if self.set_reverb(reverb).is_ok() {
            self.run_playback_counter_task().await
        }
    }

    #[inline]
    fn set_fade_in(&mut self, fade_in: Duration) -> Result<()> {
        self.playback_params.set_fade_in(fade_in);
        let src = Source::buffered(Source::fade_in(self.get_buffered_source()?, fade_in));

        self.sink.stop();

        let pos = *self.playback_position_controller.position.read().unwrap();
        self.seek_to(pos)?;

        self.sink.append(src);
        Ok(())
    }

    #[inline]
    pub async fn set_fade_in_with_counter_task(&mut self, fade_in: Duration) {
        if self.set_fade_in(fade_in).is_ok() {
            self.run_playback_counter_task().await
        }
    }

    #[inline]
    pub fn set_next_looping_state(&mut self) {
        self.playback_params.set_next_looping_state()
    }
}
