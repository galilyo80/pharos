# Pharos — EV Intelligence

Own-brand Android companion & tweak app for BYD DiLink head units (Egypt market),
built in Kotlin + Jetpack Compose. Designed with a Vehicle Abstraction Layer so
additional brands (e.g. Geely) can be added later without touching the UI.

## Architecture
- **shizuku/** — single privileged-shell gateway (no root needed)
- **vehicle/** — `VehicleAdapter` contract + `VehicleManager` (brand detection)
  - **byd/** — DiLink implementation (launch target)
  - **geely/** — stub adapter (GKUI/Flyme Auto — filled in once tested on a car)
- **activation/** — device id, license states, activation + subscription
- **ui/** — Compose theme + screens

## Open in Android Studio
1. Android Studio (Koala+), open this folder.
2. Let Gradle sync (needs internet for dependencies the first time).
3. Set the Pharos backend URL in `LicenseManager.BASE_URL`.
4. Run on the car head unit (or an Android 8.0+ device) with Shizuku installed.

## Status: Milestone 1 — foundation (activation + architecture + shell + shell UI)
Battery/trip/command readers are structured with clearly-marked TODOs to be
wired to confirmed live data sources on a real car (never guessed paths).
