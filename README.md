<p align="center">
  <img src="crown-ludo.png" alt="Ludo Multiplayer by CrownLudo Logo" width="140" height="140" style="border-radius: 20px;">
</p>

# Ludo Multiplayer by CrownLudo - Android App

A premium, feature-rich Ludo Game developed in Kotlin & Jetpack Compose.

## 🚀 GitHub Actions Auto-Build (APK & AAB)

This repository is configured with **GitHub Actions**. Every time code is pushed or exported to GitHub, GitHub Actions will automatically compile and generate:
- **Release APK** (`CrownLudo-Release-APK`)
- **Debug APK** (`CrownLudo-Debug-APK`)
- **Release AAB Bundle** (`CrownLudo-Release-AAB` for Google Play Console)

### How to download your APK & AAB from GitHub:
1. Push this repository to your GitHub account.
2. Go to the **Actions** tab on your GitHub repository.
3. Click on the latest workflow run named **Build Android APK & AAB Bundle**.
4. Scroll down to the **Artifacts** section at the bottom.
5. Download `CrownLudo-Release-APK`, `CrownLudo-Release-AAB`, or `CrownLudo-Debug-APK`!

## 🛠️ Manual Local Build

To build locally using Gradle:
```bash
# Build Release APK
./gradlew assembleRelease

# Build Release AAB Bundle (for Google Play Store)
./gradlew bundleRelease
