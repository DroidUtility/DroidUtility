package com.frostre1997.droidutility.core

import android.os.Build
import com.android.terminal_emulator.TerminalEmulator
import com.android.terminal_emulator.TerminalSession
import java.io.File

object TerminalManager {
    private var session: TerminalSession? = null

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

    fun startSession(): TerminalSession {
        if (session != null && session?.isRunning == true) return session!!

        val process = try {
            val hasSu = File("/system/bin/su").exists() || File("/system/xbin/su").exists()
            if (hasSu) {
                Runtime.getRuntime().exec(arrayOf("su", "-c", "sh"))
            } else {
                Runtime.getRuntime().exec(arrayOf("/data/local/tmp/rish", "-c", "sh"))
            }
        } catch (_: Exception) {
            Runtime.getRuntime().exec(arrayOf("/system/bin/sh"))
        }

        val user = getUsername()
        val device = getDeviceName()
        val ps1 = "$user@$device ~$ "

        process.outputStream.use {
            it.write("export PS1='$ps1'\n".toByteArray())
            it.flush()
        }

        val termSession = TerminalSession()
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
