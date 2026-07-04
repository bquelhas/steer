# Contributing to the Steer companion app

Thanks for helping! This is the phone half of Steer; the watch half lives in a
separate repo.

## Getting set up

1. Clone this repo and (optionally) the watch repo as a sibling checkout so the
   `bundleWatchPbw` task can find the compiled `.pbw` — see the README.
2. Point `JAVA_HOME` at a JDK that includes `javac` (Android Studio's bundled
   JBR works).
3. `./gradlew assembleDebug` should produce a clean debug APK.
4. `./gradlew test` runs the unit tests (`NaviParserTest`, etc.).

## Guidelines

- Keep code and comments in **English**.
- Follow the existing structure — small focused classes under
  `com.bquelhas.navme`, `NavPrefs` for persisted settings.
- **Protocol changes:** if you touch the phone↔watch messages, update
  `NavKeys.kt` here **and** `package.json` in the watch repo together, or the
  two halves drift out of sync.
- Don't commit signing material (`*.jks`, `*.keystore`), the bundled `.pbw`
  (it's a build artifact), or map-provider artwork — the `.gitignore` already
  covers the known cases.

## Pull requests

- One logical change per PR with a short what/why.
- Note which device / Android version and which map app you tested with.

By contributing you agree your contributions are licensed under the project's
MIT licence.
