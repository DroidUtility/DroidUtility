package com.frostre1997.droidutility.ui.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager

object PackageUtils {

    data class InstalledApp(
        val packageName: String,
        val name: String,
        val isSystem: Boolean,
        val versionName: String,
        val versionCode: Long,
        val sizeBytes: Long
    )

    fun getInstalledPackages(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        return packages.map { pkg ->
            
            val appInfo = pkg.applicationInfo
            val flags = appInfo?.flags ?: 
            
            InstalledApp(
                packageName = pkg.packageName,
                name = getAppName(pm, pkg),
                isSystem = (pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                versionName = pkg.versionName ?: "unknown",
                versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    pkg.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pkg.versionCode.toLong()
                },
                sizeBytes = pkg.applicationInfo?.sourceDir?.let { path ->
                    try {
                        java.io.File(path).length()
                    } catch (e: Exception) {
                        0L
                    }
                } ?: 0L
            )
        }.sortedBy { it.name.lowercase() }
    }

    fun getSystemPackages(context: Context): List<InstalledApp> {
        return getInstalledPackages(context).filter { it.isSystem }
    }

    fun getUserPackages(context: Context): List<InstalledApp> {
        return getInstalledPackages(context).filter { !it.isSystem }
    }

    fun searchPackages(query: String, packages: List<InstalledApp>): List<InstalledApp> {
        if (query.isBlank()) return packages
        val lowerQuery = query.lowercase()
        return packages.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.packageName.lowercase().contains(lowerQuery)
        }
    }

    fun formatSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        var size = sizeBytes.toDouble()
        var unitIndex = 0
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        return "%.1f %s".format(size, units[unitIndex])
    }

    private fun getAppName(pm: PackageManager, pkg: PackageInfo): String {
        return try {
            val appInfo = pkg.applicationInfo
            if (appInfo != null) {
                pm.getApplicationLabel(appInfo).toString()
            } else {
                pkg.packageName
            }
        } catch (e: Exception) {
            pkg.packageName
        }
    }
}
