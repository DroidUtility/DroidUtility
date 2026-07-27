package com.frostre1997.droidutility

import android.util.Log

data class DebloatResult(
    val packageName: String,
    val action: String,
    val success: Boolean,
    val message: String
)

object DebloatEngine {
    private const val TAG = "DebloatEngine"

    suspend fun disablePackage(packageName: String): DebloatResult {
        if (!ShizukuShellManager.checkAvailability())
            return DebloatResult(packageName, "disable", false, "Shizuku not available")
        if (!ShizukuShellManager.hasPermission())
            return DebloatResult(packageName, "disable", false, "Permission not granted")

        val result = ShizukuShellManager.executeCommand("pm disable-user --user 0 $packageName")
        val message = if (result.success) result.output else result.error.takeIf { it.isNotBlank() } ?: "Command failed"
        return DebloatResult(packageName, "disable", result.success, message)
    }

    suspend fun uninstallPackage(packageName: String): DebloatResult {
        if (!ShizukuShellManager.checkAvailability())
            return DebloatResult(packageName, "uninstall", false, "Shizuku not available")
        if (!ShizukuShellManager.hasPermission())
            return DebloatResult(packageName, "uninstall", false, "Permission not granted")

        val result = ShizukuShellManager.executeCommand("pm uninstall --user 0 $packageName")
        val message = if (result.success) result.output else result.error.takeIf { it.isNotBlank() } ?: "Command failed"
        return DebloatResult(packageName, "uninstall", result.success, message)
    }

    suspend fun enablePackage(packageName: String): DebloatResult {
        if (!ShizukuShellManager.checkAvailability())
            return DebloatResult(packageName, "enable", false, "Shizuku not available")
        if (!ShizukuShellManager.hasPermission())
            return DebloatResult(packageName, "enable", false, "Permission not granted")

        val result = ShizukuShellManager.executeCommand("pm enable $packageName")
        val message = if (result.success) result.output else result.error.takeIf { it.isNotBlank() } ?: "Command failed"
        return DebloatResult(packageName, "enable", result.success, message)
    }

    suspend fun clearAppData(packageName: String): DebloatResult {
        if (!ShizukuShellManager.checkAvailability())
            return DebloatResult(packageName, "clear", false, "Shizuku not available")
        if (!ShizukuShellManager.hasPermission())
            return DebloatResult(packageName, "clear", false, "Permission not granted")

        val result = ShizukuShellManager.executeCommand("pm clear $packageName")
        val message = if (result.success) result.output else result.error.takeIf { it.isNotBlank() } ?: "Command failed"
        return DebloatResult(packageName, "clear", result.success, message)
    }
}
