extern crate atomic_float;
extern crate futures;
extern crate futures_timer;
extern crate once_cell;
extern crate rodio;
extern crate tokio;

use atomic_float::AtomicF32;
use futures::future::{AbortHandle, Abortable};
use futures_timer::Delay;
use rodio::{source::Buffered, Decoder, OutputStream, OutputStreamHandle, Sink, Source};

use std::fs::File;
use std::io::BufReader;
use std::{
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
    utils::types::*,
    PlaylistTrait, StorageUtil, TrackTrait,
};

pub struct AudioPlayer {
    source_path: Option<PathBuf>,
    playback_data: Option<(OutputStreamHandle, Sink)>,
    total_duration: Duration,
    is_playing: Arc<AtomicBool>,
    playback_params: PlaybackParams,
    playback_position_controller: ARWLock<PlaybackPositionController>,
}

pub type ARWLPlayer = ARWLock<AudioPlayer>;

impl AudioPlayer {
    #[inline]
    pub async fn new(playback_params: PlaybackParams) -> Self {
        AudioPlayer {
            source_path: None,
            playback_data: None,
            is_playing: Arc::new(AtomicBool::default()),
            playback_params,
            playback_position_controller: Arc::new(RwLock::new(
                PlaybackPositionController::default().await,
            )),
            total_duration: Duration::default(),
        }
    }

    #[inline]
    async fn run_playback_control_task(
        is_playing: Arc<AtomicBool>,
        playback_position_controller: ARWLock<PlaybackPositionController>,
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
    async fn get_buffered_source(
        this: ARWLPlayer,
        jvm: AJVM,
    ) -> Result<Buffered<Decoder<BufReader<File>>>> {
        if this.read().await.source_path.is_none() {
            this.write().await.source_path = Some(
                StorageUtil::load_current_playlist(jvm)
                    .await
                    .get_cur_track()
                    .unwrap()
                    .get_path()
                    .clone(),
            );
        }

        Ok(Source::buffered(
            match Decoder::new(BufReader::new(
                match File::open(this.read().await.source_path.as_ref().unwrap().clone()) {
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
        playback_position_controller: ARWLock<PlaybackPositionController>,
        speed: Arc<AtomicF32>,
        max_duration: Duration,
        tokio_runtime: TokioRuntime,
    ) {
        tokio_runtime.spawn(AudioPlayer::run_playback_control_task(
            is_playing,
            playback_position_controller,
            speed,
            max_duration,
        ));
    }

    #[inline]
    async fn abort_playback_position_controller_tasks(this: ARWLPlayer) {
        this.write()
            .await
            .playback_position_controller
            .write()
            .await
            .task
            .as_ref()
            .map(|t| t.abort())
            .unwrap_or_default();
    }

    #[inline]
    async fn play_with_result(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        source: PathBuf,
        track_duration: Duration,
    ) -> Result<()> {
        Self::abort_playback_position_controller_tasks(this.clone()).await;
        Self::reset_on_play(this.clone(), source, track_duration).await;
        Self::save_cur_playback_pos_async(this.clone(), tokio_runtime.clone()).await;

        let source = Self::get_buffered_source(this.clone(), jvm).await?;

        let src = Source::buffered(Source::fade_in(
            Source::reverb(
                source,
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

        let (_, handle) = OutputStream::try_default().unwrap();
        let sink = Sink::try_new(&handle).unwrap();
        this.write().await.playback_data = Some((handle, sink));

        {
            let speed = this.read().await.playback_params.get_speed();
            let volume = this.read().await.playback_params.get_volume();
            let mut refer = this.write().await;
            let refer = &mut refer.playback_data.as_mut().unwrap().1;
            refer.append(src);
            refer.set_speed(speed);
            refer.set_volume(volume);
        }

        this.read().await.is_playing.store(true, Ordering::SeqCst);

        Ok(Self::run_playback_preparation_tasks(
            this.read().await.is_playing.clone(),
            this.read().await.playback_position_controller.clone(),
            this.read().await.get_speed_ref(),
            this.read().await.total_duration,
            tokio_runtime,
        )
        .await)
    }

    #[inline]
    async fn reset_on_play(this: ARWLPlayer, source: PathBuf, track_duration: Duration) {
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
            .await = Duration::default()
    }

    #[inline]
    pub async fn play(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        source: PathBuf,
        track_duration: Duration,
    ) {
        AudioPlayer::play_with_result(this, tokio_runtime, jvm, source, track_duration)
            .await
            .unwrap_or_default()
    }

    #[inline]
    pub async fn save_cur_playback_pos_async(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
    ) -> JoinHandle<()> {
        let pos = this.read().await.get_cur_playback_pos().await.as_millis() as u64;

        tokio_runtime.spawn(async move {
            StorageUtil::store_current_playback_position(pos)
                .await
                .unwrap_or_default()
        })
    }

    #[inline]
    pub async fn pause(this: ARWLPlayer, tokio_runtime: TokioRuntime) {
        {
            let this_ref = this.read().await;
            this_ref.is_playing.store(false, Ordering::SeqCst);

            if let Some(task) = &this_ref.playback_position_controller.read().await.task {
                task.abort()
            }

            this_ref.playback_data.as_ref().unwrap().1.stop();
        }

        Self::save_cur_playback_pos_async(this, tokio_runtime).await;
    }

    #[inline]
    async fn resume_with_result(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        track_duration: Duration,
    ) -> Result<()> {
        let cur_duration = this.read().await.get_cur_playback_pos().await;
        Self::seek_to_with_result(this, tokio_runtime, jvm, cur_duration, track_duration).await
    }

    #[inline]
    pub async fn resume(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        track_duration: Duration,
    ) {
        Self::resume_with_result(this, tokio_runtime, jvm, track_duration)
            .await
            .unwrap_or_default()
    }

    #[inline]
    pub async fn stop(this: ARWLPlayer, tokio_runtime: TokioRuntime) {
        {
            let this_ref = this.read().await;
            this_ref.is_playing.store(false, Ordering::SeqCst);

            if let Some(task) = &this_ref.playback_position_controller.read().await.task {
                task.abort()
            }

            this_ref
                .playback_data
                .as_ref()
                .map(|pd| pd.1.stop())
                .unwrap_or_default();
        }

        Self::save_cur_playback_pos_async(this, tokio_runtime).await;
    }

    #[inline]
    async fn seek_to_with_result(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        position: Duration,
        track_duration: Duration,
    ) -> Result<()> {
        let src = Self::get_buffered_source(this.clone(), jvm).await?;

        let src = Source::buffered(Source::skip_duration(
            Source::fade_in(
                Source::reverb(
                    src,
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
            ),
            position,
        ));

        Self::stop(this.clone(), tokio_runtime.clone()).await;

        let (_, handle) = OutputStream::try_default().unwrap();
        let sink = Sink::try_new(&handle).unwrap();

        this.write().await.playback_data = Some((handle, sink));
        this.write().await.total_duration = track_duration;

        {
            let this = this.read().await;
            let speed = this.playback_params.get_speed();
            let volume = this.playback_params.get_volume();
            let refer = &this.playback_data.as_ref().unwrap().1;

            refer.append(src);
            refer.set_speed(speed);
            refer.set_volume(volume);
        }

        Self::abort_playback_position_controller_tasks(this.clone()).await;
        this.read().await.is_playing.store(true, Ordering::SeqCst);

        *this
            .write()
            .await
            .playback_position_controller
            .write()
            .await
            .position
            .write()
            .await = position;

        Self::run_playback_preparation_tasks(
            this.read().await.is_playing.clone(),
            this.read().await.playback_position_controller.clone(),
            this.read().await.get_speed_ref(),
            this.read().await.total_duration,
            tokio_runtime.clone(),
        )
        .await;

        Ok(Self::save_cur_playback_pos_async(this, tokio_runtime)
            .await
            .await
            .unwrap_or_default())
    }

    #[inline]
    pub async fn seek_to(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        position: Duration,
        track_duration: Duration,
    ) {
        Self::seek_to_with_result(this, tokio_runtime, jvm, position, track_duration)
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
    pub async fn set_volume(this: ARWLPlayer, volume: f32) {
        this.write().await.playback_params.set_volume(volume);
        let volume = this.read().await.playback_params.get_volume();

        if let Some(ref pd) = this.read().await.playback_data {
            pd.1.set_volume(volume)
        }
    }

    #[inline]
    pub async fn set_speed(this: ARWLPlayer, speed: f32) {
        this.write().await.playback_params.set_speed(speed);
        let speed = this.read().await.playback_params.get_speed();

        if let Some(ref pd) = this.read().await.playback_data {
            pd.1.set_speed(speed)
        }
    }

    #[inline]
    pub async fn set_reverb(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        reverb: ReverbParams,
    ) -> Result<()> {
        this.write().await.playback_params.set_reverb(reverb);
        Self::abort_playback_position_controller_tasks(this.clone()).await;

        let pos = *this
            .read()
            .await
            .playback_position_controller
            .read()
            .await
            .position
            .read()
            .await;

        let src = Self::get_buffered_source(this.clone(), jvm).await?;

        let src = Source::buffered(Source::skip_duration(
            Source::reverb(src, reverb.get_duration(), reverb.get_amplitude()),
            pos,
        ));

        this.read()
            .await
            .playback_data
            .as_ref()
            .unwrap()
            .1
            .append(src);

        this.read().await.is_playing.store(true, Ordering::SeqCst);

        Ok(Self::run_playback_preparation_tasks(
            this.read().await.is_playing.clone(),
            this.read().await.playback_position_controller.clone(),
            this.read().await.get_speed_ref(),
            this.read().await.total_duration,
            tokio_runtime,
        )
        .await)
    }

    #[inline]
    pub async fn set_fade_in(
        this: ARWLPlayer,
        tokio_runtime: TokioRuntime,
        jvm: AJVM,
        fade_in: Duration,
    ) -> Result<()> {
        this.write().await.playback_params.set_fade_in(fade_in);
        AudioPlayer::abort_playback_position_controller_tasks(this.clone()).await;

        let pos = *this
            .read()
            .await
            .playback_position_controller
            .read()
            .await
            .position
            .read()
            .await;

        let src = Source::buffered(Source::skip_duration(
            Source::fade_in(
                AudioPlayer::get_buffered_source(this.clone(), jvm).await?,
                fade_in,
            ),
            pos,
        ));

        this.read()
            .await
            .playback_data
            .as_ref()
            .unwrap()
            .1
            .append(src);

        this.read().await.is_playing.store(true, Ordering::SeqCst);

        Ok(Self::run_playback_preparation_tasks(
            this.read().await.is_playing.clone(),
            this.read().await.playback_position_controller.clone(),
            this.read().await.get_speed_ref(),
            this.read().await.total_duration,
            tokio_runtime,
        )
        .await)
    }

    #[inline]
    pub async fn set_next_looping_state(this: ARWLPlayer) {
        this.write().await.playback_params.set_next_looping_state()
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
        self.playback_data.as_ref().unwrap().1.empty()
    }
}
