package com.zion.softminimalshortcut.model

data class InstalledApp(
    val label: String,
    val packageName: String,
    val activityName: String
)

data class SavedShortcut(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String,
    val targetAppName: String,
    val iconPath: String?
)
