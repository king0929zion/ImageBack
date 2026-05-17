package com.zion.softminimalshortcut.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.zion.softminimalshortcut.model.InstalledApp
import java.text.Collator
import java.util.Locale

object AppDiscovery {

    fun loadLaunchableApps(context: Context): List<InstalledApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val collator = Collator.getInstance(Locale.CHINA)
        val packageManager = context.packageManager
        val apps = queryLauncherActivities(packageManager, launcherIntent)

        return apps
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                if (activityInfo.packageName == context.packageName) {
                    return@mapNotNull null
                }

                val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                val fallbackLabel = activityInfo.packageName.substringAfterLast('.')
                val displayLabel = label.ifBlank { fallbackLabel }
                val systemApp = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                InstalledApp(
                    label = displayLabel,
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    searchKey = normalizeSearchKey(
                        listOf(
                            displayLabel,
                            activityInfo.packageName,
                            activityInfo.name,
                            fallbackLabel
                        ).joinToString(" ")
                    ),
                    isSystemApp = systemApp
                )
            }
            .distinctBy { "${it.packageName}/${it.activityName}" }
            .sortedWith { left, right ->
                when {
                    left.isSystemApp != right.isSystemApp -> {
                        if (left.isSystemApp) 1 else -1
                    }

                    else -> collator.compare(left.label, right.label)
                }
            }
    }

    private fun queryLauncherActivities(
        packageManager: PackageManager,
        launcherIntent: Intent
    ) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(
            launcherIntent,
            PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
        )
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
    }

    private fun normalizeSearchKey(value: String): String {
        return value
            .lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), "")
    }
}
