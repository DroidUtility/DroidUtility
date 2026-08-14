package com.frostre1997.droidutility.managers

import android.os.Build
import jackpal.androidterm.emulatorview.TermSession
import jackpal.androidterm.emulatorview.TerminalEmulator
import jackpal.androidterm.emulatorview.EmulatorView
import java.io.File
import java.io.IOException

object TerminalManager {
    private var session: TermSession? = null

    private fun getUsername(): String {
        return runCatching {
            val process = Runtime.getRuntime().exec(arrayOf("whoami"))
            val name = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            if (name.isNotEmpty()) name else "user"
        }.getOrElse { "user" }
    }

    private fun getDeviceName(): String {
        return Build.MODEL.ifBlank { Build.DEVICE.ifBlank { "android" } }
    }

    fun startSession(): TermSession {
        if (session != null && session?.isRunning() == true) return session!!

        // Start shell with best available privileges
        val process = try {
            val hasSu = File("/system/bin/su").exists() || File("/system/xbin/su").exists()
            if (hasSu) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "sh"))
            } else {
                try {
                    Runtime.getRuntime().exec(arrayOf("/data/local/tmp/rish", "-c", "sh"))
                } catch (_: Exception) {
                    Runtime.getRuntime().exec(arrayOf("/system/bin/sh"))
                }
            }
        } catch (_: Exception) {
            Runtime.getRuntime().exec(arrayOf("/system/bin/sh"))
        }

        // Set the prompt (cosmetic)
        val user = getUsername()
        val device = getDeviceName()
        val ps1 = "$user@$device ~$ "

        try {
            process.outputStream.use {
                it.write("export PS1='$ps1'\n".toByteArray())
                it.flush()
            }
        } catch (_: IOException) {
            // Ignore
        }

        // Create terminal session
        val termSession = TermSession()
        val emulator = TerminalEmulator(
            termSession,
            process.inputStream,
            process.outputStream
        )
        termSession.initialize(emulator, process)
        session = termSession
        return session!!
    }

    fun closeSession() {
        session?.finish()
        session = null
    }
}
