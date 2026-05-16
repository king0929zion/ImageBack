package com.zion.softminimalshortcut.shortcut

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.zion.softminimalshortcut.LaunchShortcutActivity
import com.zion.softminimalshortcut.model.SavedShortcut
import java.io.File

object ShortcutUtils {

    const val EXTRA_PACKAGE_NAME = "extra_package_name"
    const val EXTRA_ACTIVITY_NAME = "extra_activity_name"

    fun requestPinnedShortcut(context: Context, shortcut: SavedShortcut): Boolean {
        val iconCompat = createShortcutIcon(context, shortcut)
        val intent = Intent(context, LaunchShortcutActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_PACKAGE_NAME, shortcut.packageName)
            putExtra(EXTRA_ACTIVITY_NAME, shortcut.activityName)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcut.id)
            .setShortLabel(shortcut.label.take(20))
            .setLongLabel(shortcut.label)
            .setIntent(intent)
            .setIcon(iconCompat)
            .build()

        return ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }

    fun launchTargetApp(context: Context, packageName: String, activityName: String?): Boolean {
        val candidateIntents = buildList {
            if (!activityName.isNullOrBlank()) {
                add(
                    Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        component = ComponentName(packageName, activityName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    }
                )
            }
            context.packageManager.getLaunchIntentForPackage(packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }?.let(::add)
        }

        candidateIntents.forEach { intent ->
            val started = runCatching {
                context.startActivity(intent)
                true
            }.getOrDefault(false)

            if (started) {
                return true
            }
        }

        return false
    }

    private fun createShortcutIcon(context: Context, shortcut: SavedShortcut): IconCompat {
        val bitmapFromFile = shortcut.iconPath
            ?.let(::File)
            ?.takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.absolutePath) }

        val bitmap = bitmapFromFile ?: runCatching {
            context.packageManager.getApplicationIcon(shortcut.packageName).toBitmap(192, 192)
        }.getOrNull()

        return bitmap?.let(IconCompat::createWithBitmap)
            ?: IconCompat.createWithResource(context, android.R.drawable.sym_def_app_icon)
    }
}
