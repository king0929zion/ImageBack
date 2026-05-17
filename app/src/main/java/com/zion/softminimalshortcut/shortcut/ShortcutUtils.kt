package com.zion.softminimalshortcut.shortcut

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import com.zion.softminimalshortcut.R
import com.zion.softminimalshortcut.LaunchShortcutActivity
import com.zion.softminimalshortcut.model.SavedShortcut
import java.io.File

object ShortcutUtils {

    const val EXTRA_PACKAGE_NAME = "extra_package_name"
    const val EXTRA_ACTIVITY_NAME = "extra_activity_name"

    enum class PinShortcutRequestResult {
        Requested,
        Unsupported,
        Failed
    }

    fun requestPinnedShortcut(context: Context, shortcut: SavedShortcut): PinShortcutRequestResult {
        val shortcutBitmap = createShortcutBitmap(context, shortcut)
        val iconCompat = shortcutBitmap?.let(IconCompat::createWithBitmap)
            ?: IconCompat.createWithResource(context, R.mipmap.ic_shortcut_badge)
        val intent = Intent(context, LaunchShortcutActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(EXTRA_PACKAGE_NAME, shortcut.packageName)
            putExtra(EXTRA_ACTIVITY_NAME, shortcut.activityName)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, shortcut.id)
            .setShortLabel(shortcut.label.take(20))
            .setLongLabel(shortcut.label)
            .setActivity(ComponentName(context, LaunchShortcutActivity::class.java))
            .setIntent(intent)
            .setIcon(iconCompat)
            .build()

        val pinSupported = ShortcutManagerCompat.isRequestPinShortcutSupported(context)
        if (pinSupported && ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)) {
            return PinShortcutRequestResult.Requested
        }

        val legacySucceeded = requestLegacyPinnedShortcut(
            context = context,
            label = shortcut.label,
            launchIntent = intent,
            iconBitmap = shortcutBitmap
        )

        return when {
            legacySucceeded -> PinShortcutRequestResult.Requested
            pinSupported -> PinShortcutRequestResult.Failed
            else -> PinShortcutRequestResult.Unsupported
        }
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

    private fun requestLegacyPinnedShortcut(
        context: Context,
        label: String,
        launchIntent: Intent,
        iconBitmap: Bitmap?
    ): Boolean {
        return runCatching {
            val legacyIntent = Intent("com.android.launcher.action.INSTALL_SHORTCUT").apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, label)
                putExtra("duplicate", false)
                iconBitmap?.let { putExtra(Intent.EXTRA_SHORTCUT_ICON, it) }
            }
            context.sendBroadcast(legacyIntent)
            true
        }.getOrDefault(false)
    }

    private fun createShortcutBitmap(context: Context, shortcut: SavedShortcut): Bitmap? {
        val bitmapFromFile = shortcut.iconPath
            ?.let(::File)
            ?.takeIf { it.exists() }
            ?.let { BitmapFactory.decodeFile(it.absolutePath) }

        return bitmapFromFile ?: runCatching {
            context.packageManager.getApplicationIcon(shortcut.packageName).toBitmap(192, 192)
        }.getOrNull()
    }
}
