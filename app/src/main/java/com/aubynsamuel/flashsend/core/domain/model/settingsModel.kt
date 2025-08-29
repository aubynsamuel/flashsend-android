package com.aubynsamuel.flashsend.core.domain.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class SettingsState(
    val userName: String = "",
    val userStatus: String = "Online",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSize: Int = 16,
    val notificationsEnabled: Boolean = true,
    val lastSeenVisible: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val appVersion: String = "1.0.0",
)