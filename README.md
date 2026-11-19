<div align="center">

# Habit Tracker App

Build healthier routines by keeping track of habits, hydration, and mood in one Android app.

[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Build status](https://img.shields.io/badge/build-not_configured-lightgrey)](https://github.com/Malindu1221/habit-tracker-app)
[![License](https://img.shields.io/badge/license-not_specified-lightgrey)](https://github.com/Malindu1221/habit-tracker-app)

[Overview](#overview) · [Features](#features) · [Setup](#getting-started) · [Usage](#usage)

</div>

## Overview

Habit Tracker App is a native Android application for building consistent routines and keeping everyday wellness activity in view. It brings habit progress, water intake, and mood tracking together with reminders and simple statistics.

This project was built as a Mobile App Development (MAD) project to explore Android app development, local data storage, notifications, and user-facing tracking workflows.

## Features

- **Habit tracking** — Create, edit, and remove habits; set targets and record daily progress.
- **Progress and streaks** — Review habit completion and progress over time.
- **Home-screen widget** — See habit progress from an Android widget.
- **Hydration tracking** — Log water intake, check progress toward a daily goal, and view intake history.
- **Hydration reminders** — Configure reminder preferences and receive scheduled notifications.
- **Mood journal** — Record moods and browse entries in list or calendar views.
- **Statistics** — View habit, hydration, and mood summaries.
- **Onboarding and account screens** — Use the app's onboarding, login, and registration flows.
- **Settings** — Access app preferences and related options.

## Tech Stack & Architecture

### Technologies

- **Platform:** Android
- **Language:** Kotlin
- **Build system:** Gradle with Kotlin DSL and a version catalog
- **UI:** Android views and XML layouts, View Binding, AndroidX, and Material Components
- **Navigation:** Activities, Fragments, and bottom navigation
- **Local persistence:** Android `SharedPreferences`, with JSON serialization for app data
- **Background work and reminders:** WorkManager, alarm scheduling, and broadcast receivers
- **Charts:** MPAndroidChart for mood trends

### Architecture at a glance

The app is organized by responsibility: Android activities and fragments present the screens, data models represent tracked information, and `SharedPreferencesManager` provides local persistence. Reminder scheduling and delivery are handled through worker and receiver components. A widget component surfaces habit progress outside the app.

> The project currently stores app data locally using `SharedPreferences`. A remote backend or cloud synchronization is not included in this description.

## Project Structure

```text
.
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/                 # App-provided Markdown content
│       │   ├── java/com/example/hydralife/
│       │   │   ├── data/               # Models and local data repository
│       │   │   ├── onboards/           # Launch and onboarding screens
│       │   │   ├── receivers/          # Alarm and device-boot receivers
│       │   │   ├── sensors/            # Sensor-related code
│       │   │   ├── ui/                 # Activities, fragments, adapters, helpers
│       │   │   ├── widget/             # Habit progress widget
│       │   │   └── workers/            # Background work
│       │   └── res/                    # Layouts, drawables, menus, and values
│       ├── test/                       # Local unit tests
│       └── androidTest/                # Instrumented Android tests
├── gradle/
│   ├── libs.versions.toml              # Dependency and plugin catalog
│   └── wrapper/                        # Gradle wrapper configuration
├── build.gradle.kts
├── gradle.properties
├── gradlew
└── gradlew.bat
```

## Getting Started

### Prerequisites

- Android Studio with Android SDK Platform 36 installed
- JDK 17 to run the configured Android Gradle Plugin
- An Android emulator or device running Android 7.0 (API 24) or later

The app is configured with `minSdk 24` and `compileSdk 36`. Its Java/Kotlin compilation target is 11.

### Clone the repository

```bash
git clone https://github.com/Malindu1221/habit-tracker-app.git
cd habit-tracker-app
```

### Open and run in Android Studio

1. Open Android Studio and choose **Open**.
2. Select the cloned `habit-tracker-app` folder.
3. Allow Gradle to sync and install any requested Android SDK components.
4. Choose an emulator or connect an Android device.
5. Select the **app** run configuration and click **Run**.

### Build from the command line

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

On macOS or Linux:

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

## Usage

1. Launch the app and follow the onboarding flow.
2. Use the account screens to continue into the app.
3. Select a section from the bottom navigation:
   - **Habits** — Add habits, record progress, and review habit statistics.
   - **Mood** — Choose a mood and browse entries in list or calendar view.
   - **Hydration** — Log water, check your daily goal, and configure reminders.
   - **Settings** — Open app settings.
4. Grant notification or alarm permissions if you want to receive scheduled reminders.
5. Add the habit progress widget from your device's home-screen widget picker, if available.

## Roadmap

Possible future improvements:

- Add optional cloud backup and synchronization.
- Expand habit insights and progress visualizations.
- Improve accessibility and localization.
- Add more customization for reminders and tracking goals.
- Document and automate build and test checks.

## License

No license is currently specified in the repository. Add a license before redistributing or reusing this project.

## Author

**Malindu** — [GitHub profile](https://github.com/Malindu1221)

Project repository: [Malindu1221/habit-tracker-app](https://github.com/Malindu1221/habit-tracker-app)

## 📄 Project Documentation
You can view or download the complete project report here:
- [Project Documentation (PDF)](docs/MAD_Doc.pdf)