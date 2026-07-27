package com.frostre1997.droidutility

import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.shell.Shell   // now resolves
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
     * Execute a shell command via Shizuku's official shell API.
     * This uses the `shell` artifact and gives you full stdout/stderr/exit code.
     */
    suspend fun executeCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        if (!checkAvailability()) {
            return@withContext ShellResult(false, "", "Shizuku is not available. Please start Shizuku first.", -1)
        }
        if (!hasPermission()) {
            return@withContext ShellResult(false, "", "Shizuku permission not granted. Please grant permission.", -1)
        }

        return@withContext runCatching {
            val shell = Shizuku.newShell(arrayOf("sh", "-c", command))
            val exitCode = shell.waitFor()
            val stdout = StringBuilder()
            val stderr = StringBuilder()

            shell.getStdout()?.use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.forEach { stdout.appendLine(it) }
                }
            }
            shell.getStderr()?.use { input ->
                BufferedReader(InputStreamReader(input)).useLines { lines ->
                    lines.forEach { stderr.appendLine(it) }
                }
            }
            shell.close()

            ShellResult(
                success = exitCode == 0,
                output = stdout.toString().trimEnd(),
                error = stderr.toString().trimEnd(),
                exitCode = exitCode
            )
        }.getOrElse { e ->
            ShellResult(false, "", "Exception: ${e.message}", -1)
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
