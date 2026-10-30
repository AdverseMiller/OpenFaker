<p align="center">
  <img src="docs/banner.svg" alt="OpenFaker — Your apps. Your device identity." width="100%" />
</p>

<p align="center">
  <a href="https://github.com/AdverseMiller/OpenFaker/actions/workflows/build.yml"><img src="https://github.com/AdverseMiller/OpenFaker/actions/workflows/build.yml/badge.svg" alt="Build" /></a>
  <img src="https://img.shields.io/badge/Android-10%2B-8ce5c5?logo=android&amp;logoColor=white" alt="Android 10 or newer for hooks" />
  <img src="https://img.shields.io/badge/Xposed-API%20101-b9adff" alt="Xposed API 101" />
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7faeff?logo=kotlin&amp;logoColor=white" alt="Kotlin and Jetpack Compose" />
</p>

OpenFaker lets you choose the device information each app sees. Find an installed app, edit its identifiers, or select a device fingerprint from the bundled catalog. Each app has its own configuration and switches.

Rebuilt from an Android Faker APK into readable Kotlin, with the hook implementation compiled directly into the app. Account sign-in, subscriptions and VIP checks are omitted.

## Features

- **Per-app configuration** — searchable app list, individual field switches, manual editing and randomization.
- **Device fingerprints** — choose a manufacturer, model and Android version from the included catalog.
- **Identifier hooks** — Android ID, IMEI, serial, advertising and app set IDs, DRM, SIM/carrier details, Wi-Fi, Bluetooth and IPv4 values.
- **Import and export** — save and reuse configurations as JSON.
- **Material You** — light/dark themes, dynamic colors and custom palettes.
- **Hook logs** — inspect reported substitutions in the app.

## Get started

You need a rooted device running Android 10 or newer and an Xposed framework implementing **modern libxposed API 101**, such as a compatible Vector installation. Legacy Xposed-only frameworks cannot load this module. The configuration UI installs on Android 8.1+, but hooks require Android 10+.

1. Build and install the APK using the commands below, or download `OpenFaker-debug` from a successful [GitHub Actions build](https://github.com/AdverseMiller/OpenFaker/actions/workflows/build.yml) while signed into GitHub.
2. Enable **OpenFaker** in your Xposed manager and apply the scope to your selected apps.
3. Open OpenFaker, find an app, and enable the fields you want to customize. Use the **Fingerprint** tab for device presets.
4. Fully stop and restart the selected app so its new configuration takes effect.

The package name is `com.android1500.androidfaker.recovery`, so it can coexist with the original Android Faker installation. Both build types use this package name. CI APKs use a temporary debug signing key; build with a consistent local key for updates that preserve settings.

## Build

Use **JDK 21**, Android SDK **37.0**, and build tools **36.0.0**. The included wrapper downloads Gradle 9.6.1 and verifies its distribution checksum.

```sh
git clone https://github.com/AdverseMiller/OpenFaker.git
cd OpenFaker
export ANDROID_HOME=/path/to/Android/Sdk
sdkmanager 'platforms;android-37.0' 'build-tools;36.0.0'
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Android Studio can also open the repository directly. Set its Gradle JDK to 21 and let it sync. All runtime assets are included; the build does not need the original APK or recovery tools. Release signing is left to the builder.

## Source layout

| Path | Responsibility |
| --- | --- |
| `app/src/main/` | Launcher, application services, navigation and module metadata |
| `restored/src/main/kotlin/` | Screens, field editing, storage, logging and Xposed hooks |
| `restored/src/main/assets/` | Device and carrier catalog used by the editor |
| `restored/src/main/res/` | Theme resources, icons and fonts |

The repository contains production source and build inputs. Tests, diagnostic apps, decompiler output, original APKs and recovery notes are excluded.

## Verification and scope

The core flow was verified on a Pixel 9 Pro running Android API 37 with Vector 2.2: app selection, field editing, real Android ID and build-field substitutions, independent disable/restore, and persistence after restarting both apps. Other hook families have not all been verified on a physical device.

This rebuild focuses on the app list and field customization. Full profile-management screens are not included. Hooked values affect what selected app processes read; they do not rewrite hardware identifiers or the operating system. Platform permissions and API changes can affect individual hooks.

## Credits

Based on [Android Faker](https://github.com/Android1500/AndroidFaker) by Android1500. OpenFaker is a separate rebuilt project. Third-party attribution and bundled font licensing are in [NOTICE.md](NOTICE.md) and [licenses/](licenses/).
