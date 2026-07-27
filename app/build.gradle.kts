plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preReleaseType: Int,
    val preReleaseNumber: Int
)

fun parseSemVer(tag: String): SemVer {
    val clean = tag.removePrefix("v")
    val main = clean.substringBefore('-')
    val pre = clean.substringAfter('-', "")

    val parts = main.split(".")
    val major = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minor = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val patch = parts.getOrNull(2)?.toIntOrNull() ?: 0

    val (preType, preNumber) = if (pre.isBlank()) {
        9 to 0
    } else {
        val type = pre.substringBefore('.').lowercase()
        val number = pre.substringAfter('.', "0").toIntOrNull() ?: 0
        val rank = when (type) {
            "alpha" -> 1
            "beta" -> 2
            "rc" -> 3
            else -> 0
        }
        rank to number
    }

    return SemVer(major, minor, patch, preType, preNumber)
}

fun semVerToCode(v: SemVer): Int {
    val base = v.major * 1_000_000 + v.minor * 10_000 + v.patch * 100
    return if (v.preReleaseType == 9) {
        base + 99
    } else {
        base + v.preReleaseType * 10 + (v.preReleaseNumber.coerceAtMost(9))
    }
}

val gitTag = providers.exec {
    commandLine("git", "describe", "--tags", "--abbrev=0")
}.standardOutput.asText.get().trim()

val appVersionName = if (gitTag.isBlank()) "v1.0.5-beta.10" else gitTag
val parsed = parseSemVer(appVersionName)
val appVersionCode = semVerToCode(parsed)

android {
    namespace = "com.frostre1997.droidutility"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.frostre1997.droidutility"
        minSdk = 24
        targetSdk = 34
        versionCode = 10005 // or "1.0.5"
        versionName = "1.0.5-beta.6"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.7"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))

    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
