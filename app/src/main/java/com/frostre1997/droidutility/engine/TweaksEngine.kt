package com.frostre1997.droidutility.engine

import android.content.Context
import com.frostre1997.droidutility.ShizukuShellManager
import com.frostre1997.droidutility.data.Tweak
import com.frostre1997.droidutility.data.TweakConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

object TweaksEngine {
    private const val TAG = "TweaksEngine"

    suspend fun getTweaks(context: Context): List<Tweak> = withContext(Dispatchers.IO) {
        return@withContext try {
            context.assets.open("configs/tweaks.json").use { inputStream ->
                val reader = InputStreamReader(inputStream)
                val config = Gson().fromJson(reader, TweakConfig::class.java)
                config?.tweaks ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun applyTweak(tweak: Tweak): Boolean = withContext(Dispatchers.IO) {
        if (!ShizukuShellManager.checkAvailability() || !ShizukuShellManager.hasPermission()) {
            return@withContext false
        }

        // Execute the command via Shizuku
        val result = ShizukuShellManager.executeCommand(tweak.command)
        return@withContext result.success
    }
}
