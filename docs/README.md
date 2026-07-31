# DroidUtility - Documentation

## Architecture

DroidUtility uses a modular architecture designed for flexibility and ease of use:

- **Shell Layer:** `ShizukuShellManager` handles all privileged shell execution via the Shizuku API. Commands are executed with ADB or root privileges (if available).
- **Debloat Manager:** A full‑featured app manager that lists installed packages, filters by system/user/state, and performs enable/disable/uninstall actions via Shizuku.
- **UI Layer:** Built with Jetpack Compose, featuring:
  - **Home:** Shizuku status, app stats, recent activity.
  - **Terminal:** command history, arrow key navigation, and real‑time output.
  - **Shell:** with prompt and output display.
  - **Debloat:** App list with search, filters, and action buttons.
  - **Settings:** Theme picker (Light, Dark, AMOLED, System), accent color selector, UI scale, and more.
- **Setup Screen:** First‑run wizard that guides the user through granting Shizuku permission.
- **Theming:** Full Material 3 theming with dynamic color support (Android 12+), custom accent colors, and persistent storage.

---

## API Reference

### ShizukuShellManager

The core shell execution manager. All methods are suspend functions (coroutine‑friendly).

| Method | Description |
|--------|-------------|
| `checkAvailability(): Boolean` | Returns `true` if Shizuku is running and the binder is alive. |
| `hasPermission(): Boolean` | Returns `true` if Shizuku permission has been granted. |
| `requestPermission()` | Requests Shizuku permission from the user (shows system dialog). |
| `executeCommand(command: String): ShellResult` | Executes a shell command via Shizuku and returns a `ShellResult` with output, error, and exit code. |
| `executeCommands(commands: List<String>): List<ShellResult>` | Batch execution of multiple commands. |

#### ShellResult

| Field | Type | Description |
|-------|------|-------------|
| `success` | `Boolean` | `true` if exit code was 0. |
| `output` | `String` | Standard output (stdout) from the command. |
| `error` | `String` | Standard error (stderr) from the command. |
| `exitCode` | `Int` | The command’s exit code. |

---

### Debloat Manager (UI Layer)

The Debloat screen provides a full app manager interface.

| Feature | Description |
|---------|-------------|
| **Search** | Filter apps by name or package ID. |
| **Filters** | Toggle System / Enabled / Disabled chips to narrow the list. |
| **Actions** | Tap an app to expand and see **Disable**, **Enable**, or **Uninstall** buttons (Uninstall only for user apps). |
| **Real‑time updates** | After an action, the app list refreshes automatically. |

---

## Configuration

Debloat configurations are not yet implemented as JSON files; actions are performed directly via the UI. Future versions may support batch configs.

---

## Scripts

External scripts can be run from the Terminal tab (via Shizuku) or from Termux for batch operations. Place scripts in the `scripts/` directory (optional).

---

## Setup & Permissions

### First‑run Setup
- On first launch, the **Setup screen** appears.
- It checks if Shizuku is installed and running.
- Guides the user to start Shizuku and grant permission.
- Once permission is granted, the user can proceed to the main app.

### Shizuku Permission
- The app uses `ShizukuShellManager.requestPermission()` to prompt the user.
- Permission is required for all terminal commands and debloat actions.
- The status is displayed on the Home screen and in Settings.

---

## Theme System

| Theme | Description |
|-------|-------------|
| **Light** | White background, dark text. |
| **Dark** | Dark grey background, light text. |
| **AMOLED** | Pure black background, light text (saves battery on OLED screens). |
| **System** | Follows the system’s dark/light mode setting. |

### Accent Colors
Users can choose from 6 accent colors: **blue**, **green**, **red**, **purple**, **orange**, **pink**.

### Dynamic Color (Android 12+)
When enabled, the app uses `dynamicLightColorScheme` or `dynamicDarkColorScheme` to match the wallpaper’s color palette.

### UI Scale
A slider in Settings adjusts the entire app’s UI scale (density and font size) independently.

---

## Terminal & Shell

### Terminal
- Prompt: `$` (Android path).
- Command history: Use ↑/↓ arrow keys to recall previous commands.
- Output: Green command echo, white output.
- Clear button to reset the output.
- Welcome message: “Android Shell”.

### Shell
- Prompt: `$`.
- Output: Cyan prompt, white output.
- No history (simpler interface).
- Welcome message: “Android PowerShell”.

Both use `ShizukuShellManager.executeCommand()` to run commands.

---

## Building from Source

```bash
# Clone the repository
git clone https://github.com/frostre1997/DroidUtility.git
cd DroidUtility

# Build the debug APK
./gradlew assembleDebug

# Build the release APK (if you have a signing key)
./gradlew assembleRelease
```

---

## Dependencies

- Library Version
- Shizuku API: `13.1.5`
- Shizuku Provider: `13.1.5`
- Compose BOM: `2024.02.00`
- OkHttp: `4.12.0`
- Gson: `2.10.1`
- DataStore Preferences: `1.1.1`
- Kotlin Coroutines: `1.7.3`

---

## License

This project is licensed under the MIT License – see the [LICENSE](https://github.com/frostre1997/DroidUtility/blob/main/LICENSE) file for details.

---

## Contributing

Contributions are welcome! Please open an issue or pull request on GitHub.

---

Full Documentation: [README.md](https://github.com/frostre1997/DroidUtility/blob/main/README.md) | [Changelog](https://github.com/frostre1997/DroidUtility/releases)
