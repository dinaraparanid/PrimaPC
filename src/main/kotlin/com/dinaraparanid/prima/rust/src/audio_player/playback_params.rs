extern crate jni;

use crate::get_in_borders;
use jni::sys::jint;
use std::time::Duration;

#[derive(Copy, Clone, Debug)]
pub struct PlaybackParams {
    volume: f32,
    speed: f32,
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

impl Default for PlaybackParams {
    #[inline]
    fn default() -> Self {
        Self {
            volume: 1_f32,
            speed: 1_f32,
            reverb: ReverbParams::default(),
            fade_in: Duration::default(),
            looping_state: LoopingState::default(),
        }
    }
}

impl PlaybackParams {
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
            speed,
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
        self.speed
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
        self.speed = get_in_borders!(speed, 0.5, 2_f32, f32::min, f32::max)
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
