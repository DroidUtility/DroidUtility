package com.frostre1997.droidutility.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsDataStore {
    // Theme
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
    val ACCENT_COLOR = stringPreferencesKey("accent_color") // "blue", "green", "red", etc.

    // Terminal
    val TERMINAL_FONT_SIZE = floatPreferencesKey("terminal_font_size")
    val TERMINAL_FONT_FAMILY = stringPreferencesKey("terminal_font_family")

    // Custom fonts
    val CUSTOM_FONT_PATH = stringPreferencesKey("custom_font_path")

    // Experimental
    val EXPERIMENTAL_FEATURE_X = booleanPreferencesKey("experimental_feature_x")
    val EXPERIMENTAL_FEATURE_Y = booleanPreferencesKey("experimental_feature_y")

    // Debugging
    val RECORD_LOGS = booleanPreferencesKey("record_logs")
    val ENABLE_TELEMETRY = booleanPreferencesKey("enable_telemetry")
    val ENABLE_CRASH_REPORTS = booleanPreferencesKey("enable_crash_reports")

    // vFlow‑style extras
    val LANGUAGE = stringPreferencesKey("language")
    val COLORFUL_WORKFLOW_CARDS = booleanPreferencesKey("colorful_workflow_cards")
    val LIQUID_GLASS_NAV = booleanPreferencesKey("liquid_glass_nav")
    val UI_SCALE = floatPreferencesKey("ui_scale")

    // Defaults
    fun getDefaultThemeMode(): String = "SYSTEM"
    fun getDefaultTerminalFontSize(): Float = 16f
    fun getDefaultTerminalFontFamily(): String = "monospace"
    fun getDefaultCustomFontPath(): String = ""
    fun getDefaultBoolean(): Boolean = false
    fun getDefaultLanguage(): String = "en"
    fun getDefaultUIScale(): Float = 1.0f
    fun getDefaultAccentColor(): String = "blue"
}
