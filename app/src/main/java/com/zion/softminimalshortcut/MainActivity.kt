package com.zion.softminimalshortcut

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Image as GalleryImageIcon
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.zion.softminimalshortcut.MainViewModel.Route
import com.zion.softminimalshortcut.model.InstalledApp
import com.zion.softminimalshortcut.model.SavedShortcut
import com.zion.softminimalshortcut.ui.theme.Bg
import com.zion.softminimalshortcut.ui.theme.BgSoft
import com.zion.softminimalshortcut.ui.theme.BlueSoft
import com.zion.softminimalshortcut.ui.theme.Card
import com.zion.softminimalshortcut.ui.theme.CardSoft
import com.zion.softminimalshortcut.ui.theme.GreenSoft
import com.zion.softminimalshortcut.ui.theme.OrangeSoft
import com.zion.softminimalshortcut.ui.theme.PinkSoft
import com.zion.softminimalshortcut.ui.theme.PurpleSoft
import com.zion.softminimalshortcut.ui.theme.SoftMinimalShortcutTheme
import com.zion.softminimalshortcut.ui.theme.TextMuted
import com.zion.softminimalshortcut.ui.theme.TextPlaceholder
import com.zion.softminimalshortcut.ui.theme.TextPrimary
import com.zion.softminimalshortcut.ui.theme.TextSecondary
import com.zion.softminimalshortcut.ui.theme.YellowSoft
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val viewModel: MainViewModel = viewModel(factory = MainViewModel.factory(applicationContext))
            SoftMinimalShortcutTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Bg
                ) {
                    ShortcutApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun ShortcutApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onImageSelected(uri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 520.dp)
        ) {
            AnimatedContent(
                targetState = viewModel.currentRoute,
                transitionSpec = {
                    val forward = routeDepth(targetState) >= routeDepth(initialState)
                    val enter = slideInHorizontally(
                        animationSpec = tween(320),
                        initialOffsetX = { fullWidth ->
                            if (forward) fullWidth / 5 else -fullWidth / 5
                        }
                    ) + fadeIn(tween(260))
                    val exit = slideOutHorizontally(
                        animationSpec = tween(240),
                        targetOffsetX = { fullWidth ->
                            if (forward) -fullWidth / 6 else fullWidth / 6
                        }
                    ) + fadeOut(tween(180))

                    enter
                        .togetherWith(exit)
                        .using(SizeTransform(clip = false))
                },
                label = "route-transition"
            ) { route ->
                when (route) {
                    Route.Home -> HomeScreen(
                        shortcuts = viewModel.shortcuts,
                        onCreateClick = viewModel::startCreateFlow,
                        onLaunchShortcut = { shortcut ->
                            if (!viewModel.launchShortcut(shortcut)) {
                                Toast.makeText(context, "目标 App 无法启动", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )

                    Route.Create -> CreateScreen(
                        selectedApp = viewModel.selectedApp,
                        shortcutName = viewModel.shortcutName,
                        selectedImageUri = viewModel.selectedImageUri,
                        onBack = { viewModel.navigate(Route.Home) },
                        onPickImage = { imagePicker.launch("image/*") },
                        onOpenAppSelector = { viewModel.navigate(Route.SelectApp) },
                        onShortcutNameChange = viewModel::updateShortcutName,
                        onSave = {
                            when (val result = viewModel.saveShortcut()) {
                                is MainViewModel.SaveResult.Invalid -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                }

                                is MainViewModel.SaveResult.Success -> {
                                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    Route.SelectApp -> SelectAppScreen(
                        query = viewModel.searchQuery,
                        apps = viewModel.filteredApps,
                        isLoading = viewModel.isLoadingApps,
                        loadFailed = viewModel.appLoadFailed,
                        totalCount = viewModel.installedApps.size,
                        onBack = { viewModel.navigate(Route.Create) },
                        onQueryChange = viewModel::updateSearchQuery,
                        onRetry = viewModel::refreshInstalledApps,
                        onSelectApp = viewModel::selectApp
                    )
                }
            }
        }
    }
}

private fun routeDepth(route: Route): Int = when (route) {
    Route.Home -> 0
    Route.Create -> 1
    Route.SelectApp -> 2
}

@Composable
private fun HomeScreen(
    shortcuts: List<SavedShortcut>,
    onCreateClick: () -> Unit,
    onLaunchShortcut: (SavedShortcut) -> Unit
) {
    val shortcutColors = listOf(GreenSoft, OrangeSoft, YellowSoft, BlueSoft, PinkSoft, PurpleSoft)
    val placeholderCards = remember {
        listOf(
            PlaceholderCard("微信", Icons.Rounded.ChatBubbleOutline, GreenSoft),
            PlaceholderCard("网易云", Icons.Rounded.MusicNote, OrangeSoft),
            PlaceholderCard("备忘录", Icons.Rounded.Article, YellowSoft),
            PlaceholderCard("相机", Icons.Rounded.CameraAlt, BlueSoft)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "快捷方式工作台",
                    color = TextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (shortcuts.isEmpty()) {
                        "把常用 App 做成更干净的桌面入口"
                    } else {
                        "点一下就能直达你常用的目标应用"
                    },
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            CircularIconButton(
                icon = Icons.Rounded.Add,
                onClick = onCreateClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color(0x14503228),
                    spotColor = Color(0x14503228)
                )
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            Card,
                            Color(0xFFFFF3ED)
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "已创建快捷方式",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = shortcuts.size.toString(),
                        color = TextPrimary,
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 46.sp
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(
                        text = "个",
                        color = TextMuted,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "系统会保留一个来源角标，我已经把它切到近乎透明的母图标思路上。",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(modifier = Modifier.height(22.dp))
                PrimaryButton(text = "创建快捷方式", icon = Icons.Rounded.Add, onClick = onCreateClick)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        SectionHeader(
            title = if (shortcuts.isEmpty()) "快捷方式灵感" else "我的快捷方式",
            subtitle = if (shortcuts.isEmpty()) "先做一个试试看，后面会越来越顺手。" else "长得更简洁，点起来更直接。"
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 152.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (shortcuts.isEmpty()) {
                itemsIndexed(placeholderCards) { _, item ->
                    ShortcutTile(
                        title = item.title,
                        background = item.color,
                        leading = { TileVectorIcon(icon = item.icon) },
                        onClick = onCreateClick,
                        onPlay = onCreateClick
                    )
                }
            } else {
                itemsIndexed(shortcuts, key = { _, item -> item.id }) { index, shortcut ->
                    ShortcutTile(
                        title = shortcut.label,
                        background = shortcutColors[index % shortcutColors.size],
                        leading = { TileShortcutIcon(shortcut = shortcut) },
                        onClick = { onLaunchShortcut(shortcut) },
                        onPlay = { onLaunchShortcut(shortcut) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateScreen(
    selectedApp: InstalledApp?,
    shortcutName: String,
    selectedImageUri: Uri?,
    onBack: () -> Unit,
    onPickImage: () -> Unit,
    onOpenAppSelector: () -> Unit,
    onShortcutNameChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                size = 44.dp,
                onClick = onBack
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column {
                Text(
                    text = "创建快捷方式",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "把图标、名字和目标应用一次性配好",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFF7E7DF),
                            BgSoft
                        )
                    )
                )
                .clickable(onClick = onPickImage)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val previewSize by animateDpAsState(
                    targetValue = if (selectedImageUri == null) 88.dp else 96.dp,
                    label = "preview-size"
                )

                Box(
                    modifier = Modifier
                        .size(previewSize)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color.White.copy(alpha = 0.72f))
                        .border(
                            width = 1.5.dp,
                            color = Color.White.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Icon(
                            imageVector = Icons.Rounded.GalleryImageIcon,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(34.dp)
                        )
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(28.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = if (selectedImageUri == null) "点击上传自定义图片" else "图标已就位，点一下可以重选",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "快捷方式的来源角标会改成近乎透明的母图标，不再是一块明显的黑块。",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        SectionHeader(
            title = "目标与命名",
            subtitle = "选择目标 App 后，会自动带入名称，你也可以再改。"
        )

        Spacer(modifier = Modifier.height(14.dp))

        InputRow(onClick = onOpenAppSelector) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIconBox(
                    packageName = selectedApp?.packageName,
                    modifier = Modifier.size(40.dp),
                    container = Color.White.copy(alpha = 0.66f),
                    iconSize = 22.dp
                )
                Spacer(modifier = Modifier.size(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedApp?.label ?: "选择目标 App",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = selectedApp?.packageName ?: "打开应用目录并选择跳转目标",
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        InputRow {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "快捷方式名称",
                    color = TextMuted,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                BasicTextField(
                    value = shortcutName,
                    onValueChange = onShortcutNameChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (shortcutName.isBlank()) {
                            Text(
                                text = "填写快捷方式名称",
                                color = TextPlaceholder,
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        InfoCard(
            icon = Icons.Rounded.Info,
            text = "系统角标还会保留，但它会取用我们单独指定的透明来源活动，而不是把你看到的黑底母图标继续带出来。"
        )

        Spacer(modifier = Modifier.height(30.dp))

        PrimaryButton(
            text = "保存快捷方式",
            icon = Icons.Rounded.AutoAwesome,
            onClick = onSave
        )
    }
}

@Composable
private fun SelectAppScreen(
    query: String,
    apps: List<InstalledApp>,
    isLoading: Boolean,
    loadFailed: Boolean,
    totalCount: Int,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onSelectApp: (InstalledApp) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                size = 44.dp,
                onClick = onBack
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "选择目标 App",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isLoading) "正在读取本机应用目录…" else "已发现 $totalCount 个可启动应用",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
            CircularIconButton(
                icon = Icons.Rounded.Refresh,
                size = 42.dp,
                onClick = onRetry
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        SearchField(
            value = query,
            onValueChange = onQueryChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricPill(
                icon = Icons.Rounded.Apps,
                label = if (query.isBlank()) "全部应用" else "搜索结果",
                value = apps.size.toString(),
                modifier = Modifier.weight(1f)
            )
            MetricPill(
                icon = Icons.Rounded.AutoAwesome,
                label = "加载状态",
                value = when {
                    isLoading -> "读取中"
                    loadFailed -> "失败"
                    else -> "完成"
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> LoadingAppList()
            loadFailed -> EmptyState(
                title = "应用目录读取失败",
                message = "再试一次就好。如果系统刚装完应用，刷新一下也能把目录重新拉齐。",
                actionLabel = "重新读取",
                onAction = onRetry
            )

            apps.isEmpty() -> EmptyState(
                title = if (query.isBlank()) "这里还没有拿到应用目录" else "没搜到匹配的应用",
                message = if (query.isBlank()) {
                    "你可以先刷新一次，或者稍后再回来选。"
                } else {
                    "换应用名、包名关键词，或者清空搜索词试试。"
                },
                actionLabel = if (query.isBlank()) "刷新目录" else "重新读取",
                onAction = onRetry
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(apps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                    AppRow(app = app, onClick = { onSelectApp(app) })
                }
            }
        }
    }
}

@Composable
private fun AppRow(
    app: InstalledApp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconBox(packageName = app.packageName)
        Spacer(modifier = Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = app.packageName,
                color = TextMuted,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        AnimatedVisibility(visible = app.isSystemApp) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.72f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "系统",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LoadingAppList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(8) { index ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(74.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        if (index % 2 == 0) CardSoft else Color(0xFFF4E4DC)
                    )
            )
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Card)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(BgSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = TextMuted,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
private fun ShortcutTile(
    title: String,
    background: Color,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    onPlay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.06f)
            .clip(RoundedCornerShape(28.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            leading()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.35f))
                .clickable(onClick = onPlay),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF0DDD6))
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.size(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                if (value.isBlank()) {
                    Text(
                        text = "搜索应用名称、包名…",
                        color = TextPlaceholder,
                        fontSize = 15.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            color = TextMuted,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun MetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.52f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(BgSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column {
            Text(
                text = label,
                color = TextMuted,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.56f))
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(BgSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun TileVectorIcon(icon: ImageVector) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TextPrimary.copy(alpha = 0.8f),
        modifier = Modifier.size(24.dp)
    )
}

@Composable
private fun TileShortcutIcon(shortcut: SavedShortcut) {
    val customFile = remember(shortcut.iconPath) {
        shortcut.iconPath?.let(::File)?.takeIf { it.exists() }
    }

    if (customFile != null) {
        AsyncImage(
            model = customFile,
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        AppIconBox(
            packageName = shortcut.packageName,
            modifier = Modifier.size(30.dp),
            container = Color.Transparent,
            iconSize = 30.dp
        )
    }
}

@Composable
private fun AppIconBox(
    packageName: String?,
    modifier: Modifier = Modifier.size(44.dp),
    container: Color = BgSoft,
    iconSize: Dp = 24.dp
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        packageName?.let { targetPackage ->
            runCatching {
                context.packageManager.getApplicationIcon(targetPackage).toImageBitmap()
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                painter = BitmapPainter(bitmap),
                contentDescription = null,
                modifier = Modifier.size(iconSize)
            )
        } else {
            Icon(
                imageVector = Icons.Rounded.GalleryImageIcon,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun InputRow(
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .height(72.dp)
        .clip(RoundedCornerShape(28.dp))
        .background(Color(0xFFF0DDD6))
        .padding(horizontal = 20.dp)

    Row(
        modifier = if (onClick != null) {
            baseModifier.clickable(onClick = onClick)
        } else {
            baseModifier
        },
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun PrimaryButton(
    text: String,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val buttonModifier = Modifier
        .fillMaxWidth()
        .height(58.dp)
        .shadow(
            elevation = 14.dp,
            shape = RoundedCornerShape(29.dp),
            ambientColor = Color(0x2E000000),
            spotColor = Color(0x2E000000)
        )
        .clip(RoundedCornerShape(29.dp))
        .background(TextPrimary)
        .clickable(onClick = onClick)

    Row(
        modifier = buttonModifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { leadingIcon ->
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
        }

        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CircularIconButton(
    icon: ImageVector,
    size: Dp = 48.dp,
    onClick: () -> Unit
) {
    val buttonModifier = Modifier
        .size(size)
        .shadow(
            elevation = 6.dp,
            shape = CircleShape,
            ambientColor = Color(0x14000000),
            spotColor = Color(0x14000000)
        )
        .clip(CircleShape)
        .background(Card)
        .clickable(onClick = onClick)

    Box(modifier = buttonModifier, contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(if (size <= 44.dp) 20.dp else 22.dp)
        )
    }
}

private data class PlaceholderCard(
    val title: String,
    val icon: ImageVector,
    val color: Color
)

private fun Drawable.toImageBitmap(): ImageBitmap {
    return toBitmap(256, 256).asImageBitmap()
}
