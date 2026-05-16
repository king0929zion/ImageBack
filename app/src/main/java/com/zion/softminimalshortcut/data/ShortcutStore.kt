package com.zion.softminimalshortcut.data

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.zion.softminimalshortcut.model.SavedShortcut
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class ShortcutStore(private val context: Context) {

    private val prefs = context.getSharedPreferences("shortcut_store", Context.MODE_PRIVATE)
    private val iconDirectory = File(context.filesDir, "shortcut_icons")

    init {
        if (!iconDirectory.exists()) {
            iconDirectory.mkdirs()
        }
    }

    fun loadShortcuts(): List<SavedShortcut> {
        val raw = prefs.getString(KEY_SHORTCUTS, "[]") ?: "[]"
        val array = JSONArray(raw)
        return buildList {
            repeat(array.length()) { index ->
                val item = array.optJSONObject(index) ?: return@repeat
                add(
                    SavedShortcut(
                        id = item.optString("id"),
                        label = item.optString("label"),
                        packageName = item.optString("packageName"),
                        activityName = item.optString("activityName"),
                        targetAppName = item.optString("targetAppName"),
                        iconPath = item.optString("iconPath").ifBlank { null }
                    )
                )
            }
        }
    }

    fun saveShortcut(shortcut: SavedShortcut): List<SavedShortcut> {
        val updated = listOf(shortcut) + loadShortcuts().filterNot { it.id == shortcut.id }

        val array = JSONArray()
        updated.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("label", item.label)
                    put("packageName", item.packageName)
                    put("activityName", item.activityName)
                    put("targetAppName", item.targetAppName)
                    put("iconPath", item.iconPath.orEmpty())
                }
            )
        }
        prefs.edit().putString(KEY_SHORTCUTS, array.toString()).apply()
        return updated
    }

    fun copyIconFromUri(sourceUri: Uri): String? {
        val extension = resolveExtension(sourceUri)
        val targetFile = File(iconDirectory, "${UUID.randomUUID()}.$extension")
        return runCatching {
            val inputStream = context.contentResolver.openInputStream(sourceUri) ?: return null
            inputStream.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        }.getOrNull()
    }

    private fun resolveExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(mimeType)
            ?.takeIf { it.isNotBlank() }
            ?: "png"
    }

    companion object {
        private const val KEY_SHORTCUTS = "saved_shortcuts"
    }
}
