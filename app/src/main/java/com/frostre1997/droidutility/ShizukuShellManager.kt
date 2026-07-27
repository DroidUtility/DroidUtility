package com.frostre1997.droidutility

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuShellManager {
    private const val TAG = "ShizukuShellManager"
    private const val REQUEST_CODE = 1001

    @Volatile
    private var isBinderReceived = false
    @Volatile
    private var isPermissionGranted = false

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        isBinderReceived = true
        isPermissionGranted = hasPermission()
        Log.i(TAG, "Shizuku binder received")
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        isBinderReceived = false
        isPermissionGranted = false
        Log.w(TAG, "Shizuku binder dead")
    }

    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == REQUEST_CODE) {
            isPermissionGranted = grantResult == PackageManager.PERMISSION_GRANTED
            Log.i(TAG, "Permission result: " + if (isPermissionGranted) "granted" else "denied")
        }
    }

    init {
        registerListeners()
    }

    fun registerListeners() {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
            Shizuku.addRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register Shizuku listeners", e)
        }
    }

    fun unregisterListeners() {
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
            Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister Shizuku listeners", e)
        }
    }

    fun checkAvailability(): Boolean {
        return runCatching { Shizuku.pingBinder() }.getOrDefault(false)
    }

    fun hasPermission(): Boolean {
        if (!checkAvailability() || Shizuku.isPreV11()) return false
        return runCatching {
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)
    }

    fun requestPermission() {
        if (!checkAvailability() || Shizuku.isPreV11()) {
            Log.w(TAG, "Shizuku initialization or version check failed")
            return
        }
        if (hasPermission()) {
            isPermissionGranted = true
            return
        }
        runCatching {
            Shizuku.requestPermission(REQUEST_CODE)
        }.onFailure { e ->
            Log.e(TAG, "Failed to request permission", e)
        }
    }

    /**
     * Execute a shell command using `rish` (Shizuku's built‑in shell).
     * If `rish` is not available, falls back to `sh` (no root/ADB).
     */
    suspend fun executeCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        if (!checkAvailability() || !hasPermission()) {
            // No Shizuku – fallback to `sh` (app UID only)
            return@withContext runCatching {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                ShellResult(
                    success = exitCode == 0,
                    output = stdout.trimEnd(),
                    error = stderr.trimEnd(),
                    exitCode = exitCode
                )
            }.getOrElse { e ->
                ShellResult(false, "", "Exception: ${e.message}", -1)
            }
        }

        // Try `rish` first
        return@withContext runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("/data/local/tmp/rish", "-c", command))
            val exitCode = process.waitFor()
            val stdout = process.inputStream.bufferedReader().readText()
            val stderr = process.errorStream.bufferedReader().readText()
            ShellResult(
                success = exitCode == 0,
                output = stdout.trimEnd(),
                error = stderr.trimEnd(),
                exitCode = exitCode
            )
        }.getOrElse { rishError ->
            // `rish` failed – fallback to `sh`
            runCatching {
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
                val exitCode = process.waitFor()
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                ShellResult(
                    success = exitCode == 0,
                    output = stdout.trimEnd(),
                    error = stderr.trimEnd(),
                    exitCode = exitCode
                )
            }.getOrElse { shError ->
                ShellResult(false, "", "rish: ${rishError.message}, sh: ${shError.message}", -1)
            }
        }
    }

    suspend fun executeCommands(commands: List<String>): List<ShellResult> {
        return commands.map { executeCommand(it) }
    }

    data class ShellResult(
        val success: Boolean,
        val output: String,
        val error: String,
        val exitCode: Int
    )
}

fun ShizukuShellManager.ShellResult.displayText(): String = buildString {
    appendLine("Exit code: $exitCode")
    appendLine()
    if (output.isNotBlank()) {
        appendLine("--- STDOUT ---")
        appendLine(output)
    }
    if (error.isNotBlank()) {
        appendLine("--- STDERR ---")
        appendLine(error)
    }
    if (output.isBlank() && error.isBlank()) append("(no output)")
}
