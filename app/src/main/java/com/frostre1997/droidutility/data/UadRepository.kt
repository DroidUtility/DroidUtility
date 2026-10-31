package com.frostre1997.droidutility.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

object UadRepository {
    private const val UAD_URL = "https://raw.githubusercontent.com/Universal-Debloater-Alliance/universal-android-debloater-next-generation/main/resources/assets/uad_lists.json"
    
    private var cachedList: Map<String, UadInfo>? = null

    suspend fun fetchUadList(): Map<String, UadInfo> = withContext(Dispatchers.IO) {
        if (cachedList != null) return@withContext cachedList!!

        val client = OkHttpClient()
        val request = Request.Builder().url(UAD_URL).build()
        
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val jsonString = response.body?.string()
                val type = object : TypeToken<Map<String, UadInfo>>() {}.type
                val list: Map<String, UadInfo> = Gson().fromJson(jsonString, type)
                cachedList = list
                return@withContext list
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyMap()
    }
}
