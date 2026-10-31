package com.frostre1997.droidutility.data

import com.google.gson.annotations.SerializedName

enum class RemovalRecommendation {
    RECOMMENDED, ADVANCED, EXPERT, UNSAFE, UNLISTED
}

data class UadInfo(
    @SerializedName("id") val id: String,
    @SerializedName("list") val list: String,
    @SerializedName("description") val description: String,
    @SerializedName("removal") val removal: String
)

data class BloatAppModel(
    val appName: String,
    val packageName: String,
    val isSystemApp: Boolean,
    val isUninstalled: Boolean,
    val icon: android.graphics.drawable.Drawable?,
    val uadInfo: UadInfo?
) {
    val recommendation: RemovalRecommendation
        get() = when (uadInfo?.removal?.lowercase()) {
            "recommended" -> RemovalRecommendation.RECOMMENDED
            "advanced" -> RemovalRecommendation.ADVANCED
            "expert" -> RemovalRecommendation.EXPERT
            "unsafe" -> RemovalRecommendation.UNSAFE
            else -> RemovalRecommendation.UNLISTED
        }
}
