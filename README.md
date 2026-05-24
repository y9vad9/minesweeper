[Українською](README.uk.md)

# Minesweeper

A cross-platform Minesweeper game built with Kotlin Multiplatform and Compose Multiplatform. Runs on desktop, Android, iOS and the web from a single shared codebase.

![Preview](docs/images/preview.png)

## Features

- Classic Minesweeper rules with first-click safety and flood-fill reveal.
- Easy, Medium and Hard presets, plus custom board sizes.
- Shareable game codes and seeds, so the same board can be replayed.
- Flag mode, chording, and a game history with best times.
- Two skins (Minimal and Classic), light/dark themes, and English/Ukrainian locales.
- In-progress games are saved and restored across app restarts.

## Modules

- `logic` — game rules and core models, no UI or platform code.
- `data` — persistence (settings, history, saved game) per platform.
- `ui` — shared Compose UI and FlowMVI stores.
- `desktop`, `android`, `ios`, `web` — platform entry points.

## Requirements

- JDK 21
- Android SDK (for the Android target)
- Xcode (for the iOS target)

## Running

Desktop:

```
./gradlew :desktop:run
```

Web (opens a browser):

```
./gradlew :web:wasmJsBrowserDevelopmentRun
```

Android (device or emulator connected):

```
./gradlew :android:installDebug
```

iOS: open `iosApp` in Xcode and run on a simulator or device.

## Tests

```
./gradlew jvmTest
```

## Tech stack

Kotlin Multiplatform, Compose Multiplatform, FlowMVI, Koin, SQLDelight.
