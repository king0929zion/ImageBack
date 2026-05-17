package com.zion.softminimalshortcut

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zion.softminimalshortcut.data.AppDiscovery
import com.zion.softminimalshortcut.data.ShortcutStore
import com.zion.softminimalshortcut.model.InstalledApp
import com.zion.softminimalshortcut.model.SavedShortcut
import com.zion.softminimalshortcut.shortcut.ShortcutUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    var isLoadingApps by mutableStateOf(false)
        private set

    var appLoadFailed by mutableStateOf(false)
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
        refreshInstalledApps()
    }

    val filteredApps: List<InstalledApp>
        get() = if (searchQuery.isBlank()) {
            installedApps
        } else {
            val normalizedQuery = normalizeSearchKey(searchQuery)
            installedApps
                .filter { it.searchKey.contains(normalizedQuery) }
                .sortedWith(
                    compareBy<InstalledApp> {
                        when {
                            it.label.equals(searchQuery.trim(), ignoreCase = true) -> 0
                            normalizeSearchKey(it.label).startsWith(normalizedQuery) -> 1
                            it.packageName.lowercase().startsWith(searchQuery.trim().lowercase()) -> 2
                            else -> 3
                        }
                    }.thenBy { it.label.lowercase() }
                )
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

    fun refreshInstalledApps() {
        viewModelScope.launch {
            isLoadingApps = true
            appLoadFailed = false
            val apps = runCatching {
                withContext(Dispatchers.IO) {
                    AppDiscovery.loadLaunchableApps(appContext)
                }
            }.getOrElse {
                appLoadFailed = true
                emptyList()
            }
            installedApps = apps
            isLoadingApps = false
        }
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
        }

        shortcuts = shortcutStore.saveShortcut(shortcut)
        return when (ShortcutUtils.requestPinnedShortcut(appContext, shortcut)) {
            ShortcutUtils.PinShortcutRequestResult.Requested -> {
                resetDraft()
                currentRoute = Route.Home
                SaveResult.Success("已发起添加请求，请在系统弹窗里确认")
            }

            ShortcutUtils.PinShortcutRequestResult.Unsupported -> {
                SaveResult.Invalid("当前桌面不支持直接添加快捷方式，或未开放该能力")
            }

            ShortcutUtils.PinShortcutRequestResult.Failed -> {
                SaveResult.Invalid("快捷方式添加失败，请先允许桌面添加快捷方式后再试")
            }
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

    private fun normalizeSearchKey(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace("\\s+".toRegex(), "")
    }
}
