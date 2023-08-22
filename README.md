**Prima (PC version)**
------------------------
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Rust](https://img.shields.io/badge/rust-1.73.0-orange.svg?logo=rust)](https://www.rust-lang.org)

## **Developer**
[Paranid5](https://github.com/dinaraparanid)

## **About App**

**Prima** is a music player application that works with local music on your device.
Application is written with domain layer (audio player + database) on Rust language
and UI layer with Kotlin and Compose Multiplatform

### **Preview**

**Playback of local tracks +
audio effects +
current playlist**

<p>
    <img src="https://i.ibb.co/XpTTwKd/2023-08-23-00-02-08.png" width="125">
    &nbsp;
    <img src="https://i.ibb.co/Sn1nn6n/2023-08-23-00-30-54.png" width="125">
    &nbsp;
    <img src="https://i.ibb.co/N9G74Jf/2023-08-23-00-28-44.png" width="125">
</p>

**Ordering and filtering search**

<p>
    <img src="https://i.ibb.co/P19vL7y/2023-08-23-00-03-33.png" width="200">
    &nbsp;
    <img src="https://i.ibb.co/hZq4Y5F/2023-08-23-00-02-43.png" width="200">
</p>

**Artists and their tracks**

<p>
    <img src="https://i.ibb.co/0ZtcZvS/2023-08-23-00-03-47.png" width="200">
    &nbsp;
    <img src="https://i.ibb.co/ZSNgp5s/2023-08-23-01-00-26.png" width="200">
</p>

**Favourites**

<img src="https://i.ibb.co/6BMV8yS/2023-08-23-00-24-55.png" width="300">

## **Stack**

<ul>
    <li>General</li>
    <ul>
        <li>Kotlin 1.9.0</li>
        <li>Rust 1.73.0</li>
        <li>Java 11</li>
        <li>Cargo Gradle extension</li>
        <li>JNI Rust crate</li>
    </ul>
    <li>Concurrency</li>
    <ul>
        <li>Rust</li>
        <ul>
            <li>Tokio</li>
            <li>Futures + Futures Timer (Delay extension)</li>
            <li>Async Recursion</li>
            <li>Atomic Float</li>
            <li>Once Cell</li>
        </ul>
        <li>Kotlin</li>
        <ul>
            <li>Coroutines</li>
            <li>State Flow</li>
        </ul>
    </ul>
    <li>Databases and files</li>
    <ul>
        <li>Diesel ORM + Diesel Migrations</li>
        <li>Dirs2</li>
        <li>Serde</li>
        <li>Yaml-Rust</li>
        <li>Dotenv (Rust)</li>
        <li>Kaml + Kotlinx.Serialization</li>
    </ul>
    <li>Audio</li>
    <ul>
        <li>Rodio</li>
        <li>Chrono (duration and dates of tracks)</li>
        <li>JAudioTagger</li>
    </ul>
    <li>UI</li>
    <ul>
        <li>Compose Desktop</li>
        <li>Decompose navigation library</li>
        <li>Koin</li>
    </ul>
</ul>

**Current status**
------------------------
**Alpha V 0.3.0**

### **Implemented features:**
1. Media playback with local tracks
2. Current playlist system (add, remove, replace tracks)
3. Audio Effects: Pitch, Speed, Amplifier
4. Searching + filtering
5. Artists and their tracks
6. Favourites (tracks and artists)

### **TODO:**
1. Albums
2. Changing tracks' tags
3. Tracks' lyrics and information
4. Custom playlists
5. Settings
6. Track trimming
7. Statistics
8. *"Guess the Melody"* game

## **System Requirements**

Project is currently supported on **Linux only**.
Support for other platforms is coming soon.

## **License**

*GNU Public License V 3.0*