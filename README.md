<p align="center">
  <img src="https://raw.githubusercontent.com/DroidUtility/DroidUtility/main/image/Icon.png" alt="DroidUtility Logo" width="500" style="border-radius: 50px; box-shadow: 0 7px 8px rgba(0,0,0,0.2);">
</p>

<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Android/android2.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Kotlin/kotlin2.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Github/github1.svg">
  <a href="https://github.com/DroidUtility/DroidUtility/blob/main/LICENSE">
    <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/LicenceMIT/licencemit3.svg">
  </a>
</p>

# DroidUtility

A powerful, non-root utility suite for Android designed for system optimization, debloating, and advanced command execution. Powered by Shizuku.

---

## Features

- **True Interactive Terminal:** A fully functional terminal experience powered by a custom native engine (`libghostty-vt` + PTY). Supports real-time ANSI rendering, interactive prompts, and shell environments (utilizing a custom Zig-based shell).
- **Debloat Manager:** View all installed apps, search, and filter by System/User. Disable or uninstall bloatware with one tap using Shizuku privileges.
- **Dashboard (Home):** A clean, focused overview of your device's status, Shizuku connection, and app statistics.
- **System Status:** (Coming soon – currently removed for stability).
- **Material 3 Expressive UI:** Modern, fluid design with full dynamic color support (Material You), user-selectable accent seed colors, and adjustable UI scaling.
- **Theme Support:** Light, Dark, AMOLED (true black), and System themes with persistent storage.
- **Universal Architecture:** Built natively for all Android ABIs (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`).
- **Xposed / LSPatch Integration:** Xposed Module Implementation for more customizable apps – planned for update V1.1.0-beta.

---

## Requirements

- Android 7.0+ (API 24) – tested up to Android 16 (API 36)
- [Shizuku](https://shizuku.rikka.app/) installed and running
- No root required

---

## How to Use

1. **Install Shizuku** and start it via Wireless ADB or root.
2. **Install DroidUtility** and open it.
3. Navigate to the **Terminal** or **Debloat** tab.
4. If Shizuku is running but permission isn't granted, tap the **"Grant Shizuku Permission"** button.
5. Allow the permission in the Shizuku dialog.

### Terminal Tab
- A real shell environment. Type commands directly into the prompt.
- Features full ANSI color support, tab completion, and interactive program support.
- Powered by Shizuku for elevated privileges.

### Debloat Tab
- Shows all installed apps with their names and package IDs.
- Use the **search bar** to filter apps.
- Use the **filter chips** (All / System / User) to narrow down.
- Tap to **Uninstall** user apps or **Disable** system apps.

### Settings Tab
- Switch between **Light**, **Dark**, **AMOLED**, and **System** themes.
- Customize the app's **Accent Color** using the Material 3 color picker.
- Adjust the global **UI Scale**.
- Check Shizuku status and re-grant permissions if needed.

---

## Building from Source

Building DroidUtility requires the Android SDK, NDK, CMake, and Zig (for the native terminal engine).

```bash
# Clone the repository
git clone https://github.com/DroidUtility/DroidUtility.git
cd DroidUtility

# Build the debug APK
./gradlew assembleDebug
```

Note: The libghostty-vt native library is pre-compiled in the app/src/main/cpp/vendor/ directory. If you are modifying the native terminal code, you will need to cross-compile the Zig library manually.

---

Visit our website 🤍

https://droidutility.github.io/DroidUtility/