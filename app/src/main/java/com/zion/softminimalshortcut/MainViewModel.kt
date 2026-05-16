package com.zion.softminimalshortcut

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zion.softminimalshortcut.data.AppDiscovery
import com.zion.softminimalshortcut.data.ShortcutStore
import com.zion.softminimalshortcut.model.InstalledApp
import com.zion.softminimalshortcut.model.SavedShortcut
import com.zion.softminimalshortcut.shortcut.ShortcutUtils
import java.util.UUID

class MainViewModel(
    private val appContext: Context
) : ViewModel() {

    enum class Route {
        Home, Create, SelectApp
    }

    private val shortcutStore = ShortcutStore(appContext)

    var currentRoute by mutableStateOf(Route.Home)
        private set

    var shortcuts by mutableStateOf(emptyList<SavedShortcut>())
        private set

    var installedApps by mutableStateOf(emptyList<InstalledApp>())
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedApp by mutableStateOf<InstalledApp?>(null)
        private set

    var shortcutName by mutableStateOf("")
        private set

    var selectedImageUri by mutableStateOf<Uri?>(null)
        private set

    init {
        shortcuts = shortcutStore.loadShortcuts()
        installedApps = AppDiscovery.loadLaunchableApps(appContext)
    }

    val filteredApps: List<InstalledApp>
        get() = if (searchQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.label.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
            }
        }

    fun navigate(route: Route) {
        currentRoute = route
    }

    fun startCreateFlow() {
        resetDraft()
        currentRoute = Route.Create
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun updateShortcutName(name: String) {
        shortcutName = name
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri = uri
    }

    fun selectApp(app: InstalledApp) {
        selectedApp = app
        shortcutName = app.label
        currentRoute = Route.Create
    }

    fun launchShortcut(shortcut: SavedShortcut): Boolean {
        return ShortcutUtils.launchTargetApp(
            context = appContext,
            packageName = shortcut.packageName,
            activityName = shortcut.activityName
        )
    }

    fun saveShortcut(): SaveResult {
        val app = selectedApp ?: return SaveResult.Invalid("请选择目标 App")
        val trimmedName = shortcutName.trim()

        if (trimmedName.isBlank()) {
            return SaveResult.Invalid("请填写快捷方式名称")
        }

        val iconPath = selectedImageUri?.let(shortcutStore::copyIconFromUri)

        val shortcut = SavedShortcut(
            id = UUID.randomUUID().toString(),
            label = trimmedName,
            packageName = app.packageName,
            activityName = app.activityName,
            targetAppName = app.label,
            iconPath = iconPath
        )

        shortcuts = shortcutStore.saveShortcut(shortcut)
        val created = ShortcutUtils.requestPinnedShortcut(appContext, shortcut)
        resetDraft()
        currentRoute = Route.Home

        return if (created) {
            SaveResult.Success("已请求创建桌面快捷方式")
        } else {
            SaveResult.Success("已保存配置，当前桌面可能不支持固定快捷方式")
        }
    }

    private fun resetDraft() {
        selectedApp = null
        shortcutName = ""
        selectedImageUri = null
        searchQuery = ""
    }

    sealed interface SaveResult {
        data class Success(val message: String) : SaveResult
        data class Invalid(val message: String) : SaveResult
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(context.applicationContext) as T
            }
        }
    }
}
