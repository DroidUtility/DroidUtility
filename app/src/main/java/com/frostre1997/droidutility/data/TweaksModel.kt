package com.frostre1997.droidutility.data

import com.google.gson.annotations.SerializedName

data class Tweak(
    @SerializedName("name") val name: String,
    @SerializedName("command") val command: String
)

data class TweakConfig(
    @SerializedName("tweaks") val tweaks: List<Tweak>
)
