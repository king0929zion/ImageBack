package com.zion.softminimalshortcut.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.os.Process
import com.zion.softminimalshortcut.model.InstalledApp
import java.text.Collator
import java.util.Locale

object AppDiscovery {

    fun loadLaunchableApps(context: Context): List<InstalledApp> {
        val collator = Collator.getInstance(Locale.CHINA)
        val launcherApps = context.getSystemService(LauncherApps::class.java)
        val apps = launcherApps?.getActivityList(null, Process.myUserHandle()).orEmpty()

        return apps
            .mapNotNull { activityInfo ->
                val packageName = activityInfo.applicationInfo.packageName
                if (packageName == context.packageName) {
                    return@mapNotNull null
                }

                val label = activityInfo.label?.toString()?.trim().orEmpty()
                val fallbackLabel = packageName.substringAfterLast('.')
                val displayLabel = label.ifBlank { fallbackLabel }
                val systemApp = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val componentKey = "${activityInfo.componentName.packageName}/${activityInfo.componentName.className}"

                InstalledApp(
                    label = displayLabel,
                    packageName = packageName,
                    activityName = activityInfo.componentName.className,
                    searchKey = normalizeSearchKey(
                        listOf(
                            displayLabel,
                            packageName,
                            activityInfo.componentName.className,
                            fallbackLabel
                        ).joinToString(" ")
                    ),
                    isSystemApp = systemApp,
                    componentKey = componentKey
                )
            }
            .distinctBy { it.componentKey }
            .sortedWith { left, right ->
                when {
                    left.isSystemApp != right.isSystemApp -> {
                        if (left.isSystemApp) 1 else -1
                    }

                    else -> collator.compare(left.label, right.label)
                }
            }
    }

    private fun normalizeSearchKey(value: String): String {
        return value
            .lowercase(Locale.ROOT)
            .replace("\\s+".toRegex(), "")
    }
}
