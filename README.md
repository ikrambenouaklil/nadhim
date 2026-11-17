# Nadhim — Android app

## Project structure

```
nadhim/
├── app/                              ← THE APP (this is where you work)
│   ├── build.gradle                  app settings: SDK versions, dependencies
│   └── src/main/
│       ├── AndroidManifest.xml       app name, icon, which screen opens first
│       ├── java/com/example/myapplication/
│       │   └── MainActivity.java     the code (logic) of the main screen
│       └── res/                      resources (everything visual)
│           ├── layout/
│           │   └── activity_main.xml the main screen design
│           ├── drawable/             shapes, backgrounds, icons, logo (bg_*, check_*, logo_nadhim…)
│           ├── drawable-mdpi/        PNG images (bg_glow, bg_grid)
│           ├── font/                 Poppins fonts
│           └── values/
│               ├── colors.xml        all app colors
│               ├── strings.xml       all texts
│               └── styles.xml        app theme
│
├── build.gradle                      project-wide Gradle config (rarely touched)
├── settings.gradle                   lists the modules (just :app)
├── gradle.properties                 Gradle options
├── gradle/wrapper/, gradlew, gradlew.bat   Gradle launcher — don't delete
└── local.properties                  path to your Android SDK (not in git)
```

## What you normally edit

| I want to change…   | Edit this file                         |
|---------------------|----------------------------------------|
| How the screen looks | `res/layout/activity_main.xml`        |
| What buttons do      | `java/.../MainActivity.java`          |
| Colors               | `res/values/colors.xml`               |
| Texts                | `res/values/strings.xml`              |

## Build

```
gradlew assembleDebug
```

The `build/` and `.gradle/` folders are generated automatically when you build — they are ignored by git and safe to delete anytime.
