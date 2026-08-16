# 🛡️ DeepGuard – Focus Better. Live Better.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4.svg?logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-24%20%28Android%207.0%2B%29-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Proprietary%20%2F%20All%20Rights%20Reserved-red.svg)](./LICENSE)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-orange.svg)]()

**DeepGuard** is a digital wellbeing and focus companion engineered for Android. Built with 100% Kotlin and modern Jetpack Compose Material 3, DeepGuard empowers individuals to conquer digital distractions, build disciplined habits, and reclaim deep focus through distraction blocking and mindful focus modes.

---

## ✨ Key Features

| Feature | Description |
| :--- | :--- |
| 🛡️ **Instant & Scheduled Focus Lock** | Lock distracting apps with high-precision countdown timers (HH:MM:SS) or quick 15m/30m/1h presets. |
| 🛑 **Real-Time App Blocker** | Instantly intercepts restricted apps and presents an inspiring blocking overlay. |
| 🧠 **Mindful Focus Modes** | 5 science-backed sound & timer focus environments: **Study Focus**, **Better Sleep**, **Stress Relief**, **Anger Control**, and **Fear & Anxiety**. |
| 📊 **Smart App Management** | Easily search, toggle, and view installed applications with quick selection controls. |
| 🔒 **Tamper-Resistant Security** | Robust protection backed by Android Accessibility Services and Device Administrator permissions. |
| 🌿 **Sleek Emerald Dark UI** | Crafted according to Material Design 3 guidelines with soothing emerald accents. |

---

## 📱 App Screenshots

```
 ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
 │   01 Guard      │   │    02 Apps      │   │    03 Focus     │   │ 04 Permissions  │
 │  [00:15:00]     │   │  [App Selector] │   │  [Sound & Mode] │   │  [Shield Admin] │
 └─────────────────┘   └─────────────────┘   └─────────────────┘   └─────────────────┘
```
*(Add your exported preview image or GIF here: `assets/screenshots/preview.png`)*

---

## 🏗️ Architecture & Technology Stack

DeepGuard follows modern Android development best practices and Clean MVVM architecture:

- **Language:** 100% [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3 Design System
- **State Management:** ViewModel + `StateFlow` + `collectAsStateWithLifecycle`
- **Database / Local Cache:** [Room Database](https://developer.android.com/training/data-storage/room) with KSP
- **Background & Security Services:**
  - `AccessibilityService` (Foreground app detection and interruption)
  - `DeviceAdminReceiver` (Anti-tamper protection)
  - `ForegroundService` (Persistent background notification & lock state keeper)
- **Audio Engine:** `MediaPlayer` / `SoundPool` for mindful ambient sound loops
- **Code Shrinking & Obfuscation:** R8 / ProGuard rules with identifier renaming and resource shrinking

---

## 🚀 Getting Started & Building

### Prerequisites
- **Android Studio Ladybug | 2024.2.1** or newer
- **JDK 17** or **JDK 21**
- Android device or emulator running **Android 7.0 (API 24)** or higher

### Build Instructions
1. Clone this repository:
   ```bash
   git clone https://github.com/YOUR_USERNAME/DeepGuard.git
   cd DeepGuard
   ```
2. Open the project in Android Studio.
3. Sync Gradle dependencies:
   ```bash
   ./gradlew build
   ```
4. Run the app on your connected device/emulator.

---

## ⚖️ Intellectual Property & License Notice

```text
PROPRIETARY & CONFIDENTIAL — ALL RIGHTS RESERVED
Copyright (c) 2026 DeepGuard.
```

- **No Unauthorized Reproduction:** No individual or organization may copy, clone, distribute, decompile, or modify this code without prior written permission.
- **No Commercial Use or Derivative Works:** Creating white-labeled copies, derivative apps, or publishing this codebase on Google Play / third-party app stores is strictly prohibited.
- **DMCA Protection:** Any unauthorized duplication or distribution will be reported for immediate intellectual property removal and DMCA action.

*For licensing inquiries, enterprise deployment, or partnership requests, please contact the repository maintainer.*

---

<p align="center">Made with ❤️ for disciplined minds & deep focus.</p>
