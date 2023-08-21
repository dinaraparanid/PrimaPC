extern crate atomic_float;
extern crate jni;

use crate::{get_in_borders, ARWLStorage};
use atomic_float::AtomicF32;
use jni::sys::jint;

use std::{
    sync::{atomic::Ordering, Arc},
    time::Duration,
};

#[derive(Clone, Debug)]
pub struct PlaybackParams {
    volume: f32,
    speed: Arc<AtomicF32>,
    reverb: ReverbParams,
    fade_in: Duration,
    looping_state: LoopingState,
}

#[derive(Copy, Clone, Debug, Default)]
pub struct ReverbParams {
    duration: Duration,
    amplitude: f32,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum LoopingState {
    Playlist,
    Track,
    NoLooping,
}

impl PlaybackParams {
    #[inline]
    pub async fn default(storage_util: ARWLStorage) -> Self {
        let storage_util = storage_util.read().await;

        Self {
            volume: storage_util.load_volume(),
            speed: Arc::new(AtomicF32::new(storage_util.load_speed())),
            reverb: ReverbParams::default(),
            fade_in: Duration::default(),
            looping_state: storage_util.load_looping_state(),
        }
    }

    #[inline]
    pub fn new(
        volume: f32,
        speed: f32,
        reverb: ReverbParams,
        fade_in: Duration,
        looping_state: LoopingState,
    ) -> Self {
        Self {
            volume,
            speed: Arc::new(AtomicF32::new(speed)),
            reverb,
            fade_in,
            looping_state,
        }
    }

    #[inline]
    pub fn get_volume(&self) -> f32 {
        self.volume
    }

    #[inline]
    pub fn get_speed(&self) -> f32 {
        self.speed.load(Ordering::SeqCst)
    }

    #[inline]
    pub fn get_speed_ref(&self) -> Arc<AtomicF32> {
        self.speed.clone()
    }

    #[inline]
    pub fn get_reverb(&self) -> ReverbParams {
        self.reverb
    }

    #[inline]
    pub fn get_fade_in(&self) -> Duration {
        self.fade_in
    }

    #[inline]
    pub fn get_looping_state(&self) -> LoopingState {
        self.looping_state
    }

    #[inline]
    pub fn set_volume(&mut self, volume: f32) {
        self.volume = get_in_borders!(volume, 0_f32, 2_f32, f32::min, f32::max)
    }

    #[inline]
    pub fn set_speed(&mut self, speed: f32) {
        self.speed.store(
            get_in_borders!(speed, 0.5, 2_f32, f32::min, f32::max),
            Ordering::SeqCst,
        )
    }

    #[inline]
    pub fn set_reverb(&mut self, reverb: ReverbParams) {
        self.reverb = reverb
    }

    #[inline]
    pub fn set_fade_in(&mut self, fade_in: Duration) {
        self.fade_in = fade_in
    }

    #[inline]
    pub fn set_next_looping_state(&mut self) {
        let looping = match self.looping_state {
            LoopingState::NoLooping => LoopingState::Playlist,
            LoopingState::Track => LoopingState::NoLooping,
            LoopingState::Playlist => LoopingState::Track,
        };

        self.looping_state = looping
    }
}

impl ReverbParams {
    #[inline]
    pub fn new(duration: Duration, amplitude: f32) -> Self {
        Self {
            duration,
            amplitude,
        }
    }

    #[inline]
    pub fn get_duration(&self) -> Duration {
        self.duration
    }

    #[inline]
    pub fn get_amplitude(&self) -> f32 {
        self.amplitude
    }

    #[inline]
    pub fn set_duration(&mut self, duration: Duration) {
        self.duration = duration
    }

    #[inline]
    pub fn set_amplitude(&mut self, amplitude: f32) {
        self.amplitude = get_in_borders!(amplitude, 0_f32, 1_f32, f32::min, f32::max)
    }
}

impl Default for LoopingState {
    #[inline]
    fn default() -> Self {
        Self::Playlist
    }
}

impl From<LoopingState> for jint {
    #[inline]
    fn from(state: LoopingState) -> Self {
        match state {
            LoopingState::Playlist => 0,
            LoopingState::Track => 1,
            LoopingState::NoLooping => 2,
        }
    }
}

impl From<jint> for LoopingState {
    #[inline]
    fn from(state: jint) -> Self {
        match state {
            0 => Self::Playlist,
            1 => Self::Track,
            2 => Self::NoLooping,
            _ => unreachable!(),
        }
    }
}

impl From<LoopingState> for i64 {
    #[inline]
    fn from(state: LoopingState) -> Self {
        match state {
            LoopingState::Playlist => 0,
            LoopingState::Track => 1,
            LoopingState::NoLooping => 2,
        }
    }
}

impl From<i64> for LoopingState {
    #[inline]
    fn from(state: i64) -> Self {
        match state {
            0 => Self::Playlist,
            1 => Self::Track,
            2 => Self::NoLooping,
            _ => unreachable!(),
        }
    }
}
