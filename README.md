<p align="center">
  <img src="app/src/main/ic_launcher-playstore.png" width="120" alt="Decurion"/>
</p>

# Decurion

Android client for [Centurion](https://github.com/RakaAmburo/centurion) — a home automation and security server built on Node.js.

## What it does

Decurion connects to a self-hosted Centurion server over HTTPS and lets you:

- **Monitor status** — periodically polls the server and displays a live event list with severity levels
- **Send commands** — trigger actions on the server via voice commands or on-screen buttons
- **Push notifications** — receives instant alerts via Firebase Cloud Messaging when the server fires events
- **Error tracking** — keeps a local log of the last 10 connection errors (timeouts, network failures) so you know what happened while you were away
- **Background polling** — uses WorkManager to check the server periodically even when the app is in the background

## Architecture

```
Decurion (Android)
    │
    ├── RestClient         HTTPS + mutual TLS + token auth + exponential backoff retry
    ├── UdpTransceiver     UDP channel for low-latency local commands
    ├── CheckWorker        WorkManager background periodic polling
    ├── MyFirebaseMessagingService   FCM push notifications
    ├── EvaluateStatusResponse       Parses server status events, updates UI + error log
    └── SharedPreferencesHelper      Persists last 5 status events + last 10 errors locally
```

## Setup

1. Clone and open in Android Studio.
2. Add your `google-services.json` (Firebase project) to `app/`.
3. Place your BKS keystore as `app/src/main/res/raw/android.bks`.
4. Set your keystore password and server details in `SecuredProperties.java`.
5. Build and run on your device.

## Requirements

- Android 8.0+ (API 26)
- A running [Centurion](https://github.com/RakaAmburo/centurion) server
- Firebase project for push notifications

## Related

- **[Centurion](https://github.com/RakaAmburo/centurion)** — the Node.js server this app talks to
