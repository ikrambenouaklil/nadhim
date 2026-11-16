# Nadhim

A native Android task planner written in Java. It provides automatic task categories and priorities, configurable focus hours with suggestions learned from completed tasks, progress tracking, and scheduled reminders.

## See a browser preview

Open `preview.html` in a web browser. It works without Android Studio, a JDK, or an internet connection. The preview starts with sample tasks so you can see the screen right away; adding, completing, or deleting tasks updates the progress and saves changes in that browser. Browser reminders are shown as task details only; scheduled notifications are available in the Android app.

## Open and run

1. Install Android Studio with JDK 17 and Android SDK Platform 35.
2. Open this folder as an Android Studio project and allow Gradle to sync.
3. Run the `app` configuration on an emulator or Android device.

The project targets Android 6.0 (API 23) and requests notification permission on Android 13 and newer. Task data and availability settings are stored locally on the device.
