# Ludo Prime - Android App

A premium, feature-rich Ludo Game developed in Kotlin & Jetpack Compose.

## 🚀 GitHub Actions Auto-Build (APK & AAB)

This repository is configured with **GitHub Actions**. Every time code is pushed or exported to GitHub, GitHub Actions will automatically compile and generate:
- **Release APK** (`LudoPrime-Release-APK`)
- **Debug APK** (`LudoPrime-Debug-APK`)
- **Release AAB Bundle** (`LudoPrime-Release-AAB` for Google Play Console)

### How to download your APK & AAB from GitHub:
1. Push this repository to your GitHub account.
2. Go to the **Actions** tab on your GitHub repository.
3. Click on the latest workflow run named **Build Android APK & AAB Bundle**.
4. Scroll down to the **Artifacts** section at the bottom.
5. Download `LudoPrime-Release-APK`, `LudoPrime-Release-AAB`, or `LudoPrime-Debug-APK`!

## 🛠 Manual Local Build

To build locally using Gradle:
```bash
# Build Release APK
./gradlew assembleRelease

# Build Release AAB Bundle (for Google Play Store)
./gradlew bundleRelease
```
- The output APK will be placed in `app/build/outputs/apk/release/app-release.apk`.
- The output AAB will be placed in `app/build/outputs/bundle/release/app-release.aab`.
