package com.zion.softminimalshortcut.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.zion.softminimalshortcut.model.InstalledApp
import java.text.Collator
import java.util.Locale

object AppDiscovery {

    fun loadLaunchableApps(context: Context): List<InstalledApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val collator = Collator.getInstance(Locale.CHINA)

        return context.packageManager
            .queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                val label = resolveInfo.loadLabel(context.packageManager)?.toString().orEmpty()
                InstalledApp(
                    label = label.ifBlank { activityInfo.packageName.substringAfterLast('.') },
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name
                )
            }
            .distinctBy { it.packageName }
            .sortedWith { left, right -> collator.compare(left.label, right.label) }
    }
}
