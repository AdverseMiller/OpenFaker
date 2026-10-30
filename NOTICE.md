# Attribution

OpenFaker reconstructs an Android Faker APK into readable Kotlin source. The original application, device/carrier catalog, and recovered behavior are credited to Android1500 and the Android Faker contributors: https://github.com/Android1500/AndroidFaker.

The original package namespaces and configuration format are retained for compatibility. OpenFaker has its own name, icon, repository links and build packaging.

## Bundled fonts

The DM Sans fonts in `restored/src/main/res/font/` are by The DM Sans Project Authors and are distributed under the SIL Open Font License 1.1. See [licenses/DM-Sans-OFL.txt](licenses/DM-Sans-OFL.txt).

## Build tooling and dependencies

The Gradle wrapper is from Gradle and is distributed under Apache License 2.0; see [licenses/Gradle-Apache-2.0.txt](licenses/Gradle-Apache-2.0.txt).

Dependencies are resolved by Gradle rather than vendored into this repository. Kotlin, AndroidX, libxposed, libsu, MMKV, MaterialKolor and their transitive dependencies retain their respective licenses.
