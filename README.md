<p align="center">
  <img src="https://raw.githubusercontent.com/frostre1997/DroidUtility/main/app/src/main/res/drawable/play_store_512.png" alt="DroidUtility Logo" width="500" style="border-radius: 50px; box-shadow: 0 7px 8px rgba(0,0,0,0.2);">
</p>

<p align="center">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Android/android2.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Kotlin/kotlin2.svg">
  <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/Github/github1.svg">
  <a href="https://github.com/frostre1997/DroidUtility/blob/main/LICENSE">
    <img src="https://ziadoua.github.io/m3-Markdown-Badges/badges/LicenceMIT/licencemit3.svg">
  </a>

# DroidUtility

A powerful, non-root utility suite for Android designed for system optimization, debloating, and advanced command execution. Built for mobile-only development.

---

## Features

- **Shell Terminal:** Execute privileged shell commands via Shizuku with real-time output and history.
- **Debloat Manager:** View all installed apps, search, filter by System/User, and uninstall or disable apps with one tap.
- **System Status:** (Coming soon – currently removed for stability)
- **Theme Support:** Light, Dark, AMOLED (true black), and System themes with persistent storage.
- **Shizuku Integration:** Uses Shizuku for elevated privileges without root. Permission request is built into the app.
- **Material 3 UI:** Modern, rounded, responsive design with dark/light mode support.
- **Xsposed/LSPacth Implantation:** Xsposed Module Implemention for more customizable app's - <ins>coming in update v1.1.0-beta</ins>

---

## Requirements

- Android 7.0+ (API 24) – tested up to Android 14
- [Shizuku](https://shizuku.rikka.app/) installed and running
- No root required

---

## How to Use

1. **Install Shizuku** and start it via Wireless ADB or root.
2. **Install DroidUtility** and open it.
3. Go to the **Terminal** or **Debloat** tab.
4. If Shizuku is running but permission isn't granted, tap the **"Grant Shizuku Permission"** button.
5. Allow the permission in the Shizuku dialog.
6. Use the app:

### Terminal Tab
- Enter any shell command (e.g., `pm list packages`, `getprop`, `dumpsys battery`).
- Tap **Execute** to run it.
- Output appears in the terminal window with exit code.

### Debloat Tab
- Shows all installed apps with their names and package IDs.
- Use the **search bar** to filter apps.
- Use the **filter chips** (All / System / User) to narrow down.
- Tap **Uninstall** to remove a user app.
- Tap **Disable** to disable a system app (requires Shizuku).

### Settings Tab
- Switch between **Light**, **Dark**, **AMOLED** (true black), and **System** themes.
- Check Shizuku status (running / permission granted).
- Re‑grant permission if needed.

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/frostre1997/DroidUtility.git
cd DroidUtility

# Build the debug APK
./gradlew assembleDebug
```
## Visit my website 🤍
https://frostre1997.github.io/DroidUtility/
