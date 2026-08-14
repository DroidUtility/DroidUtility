package com.frostre1997.droidutility

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

object UpdateManager {

    private const val GITHUB_API = "https://api.github.com/repos/frostre1997/DroidUtility/releases/latest"
    private const val DOWNLOAD_FOLDER = "DroidUtility"

    data class GitHubRelease(
        val tag_name: String,
        val name: String,
        val body: String,
        val assets: List<Asset>
    )

    data class Asset(
        val name: String,
        val browser_download_url: String
    )

    suspend fun checkForUpdate(context: Context): String? {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder().url(GITHUB_API).build()
            val response = client.newCall(request).execute()
            val json = response.body?.string()

            if (json != null) {
                val release = Gson().fromJson(json, GitHubRelease::class.java)
                val latestVersion = release.tag_name.replace("v", "")
                val currentVersion = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
                } catch (e: Exception) { return "" }

                if (compareVersions(latestVersion, currentVersion) > 0) {
                    val apkAsset = release.assets.find { it.name.endsWith(".apk") }
                    apkAsset?.browser_download_url ?: release.assets.firstOrNull()?.browser_download_url
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downloadAndInstall(context: Context, downloadUrl: String) {
        val destination = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DOWNLOAD_FOLDER
        )
        if (!destination.exists()) destination.mkdirs()

        val file = File(destination, "DroidUtility.apk")
        if (file.exists()) file.delete()

        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("DroidUtility Update")
            .setDescription("Downloading latest version...")
            .setDestinationUri(Uri.fromFile(file))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("application/vnd.android.package-archive")

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)

        Thread {
            var completed = false
            while (!completed) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = manager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        completed = true
                        installApk(context, file)
                    }
                }
                cursor.close()
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun installApk(context: Context, file: File) {
        val uri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
    }
}
