package com.frostre1997.droidutility.managers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ShellManager {
    suspend fun executeCommand(command: String): ShellResult = withContext(Dispatchers.IO) {
        runCatching {
            val hasSu = File("/system/bin/su").exists() || File("/system/xbin/su").exists()
            val shellCmd = if (hasSu) {
                arrayOf("su", "-c", command)
            } else {
                try {
                    arrayOf("/data/local/tmp/rish", "-c", command)
                } catch (_: Exception) {
                    arrayOf("/system/bin/sh", "-c", command)
                }
            }

            val process = Runtime.getRuntime().exec(shellCmd)
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

    data class ShellResult(
        val success: Boolean,
        val output: String,
        val error: String,
        val exitCode: Int
    )
}
