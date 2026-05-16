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
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Image as GalleryImageIcon
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.graphics.drawable.toBitmap
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
                .widthIn(max = 480.dp)
        ) {
            when (viewModel.currentRoute) {
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
                    onBack = { viewModel.navigate(Route.Create) },
                    onQueryChange = viewModel::updateSearchQuery,
                    onSelectApp = viewModel::selectApp
                )
            }
        }
    }
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "早上好，\nZion",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp
            )

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
                .background(Card)
                .padding(28.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = PinkSoft.copy(alpha = 0.4f),
                    radius = size.minDimension * 0.32f,
                    center = Offset(x = size.width + 18.dp.toPx(), y = -14.dp.toPx())
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "已创建快捷方式",
                    color = TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = shortcuts.size.toString(),
                        color = TextPrimary,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "个",
                        color = TextMuted,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(text = "创建快捷方式", icon = Icons.Rounded.Add, onClick = onCreateClick)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val gridModifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        if (shortcuts.isEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = gridModifier
            ) {
                itemsIndexed(placeholderCards) { _, item ->
                    ShortcutTile(
                        title = item.title,
                        background = item.color,
                        leading = { TileVectorIcon(icon = item.icon) },
                        onClick = onCreateClick,
                        onPlay = onCreateClick
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = gridModifier
            ) {
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
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                size = 44.dp,
                onClick = onBack
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = "创建图标",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(BgSoft)
                .clickable(onClick = onPickImage)
                .padding(horizontal = 20.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                        .border(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Icon(
                            imageVector = Icons.Rounded.GalleryImageIcon,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "图标预览",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedImageUri == null) "点击上传自定义图片" else "已选择自定义图标",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        InputRow(onClick = onOpenAppSelector) {
            Text(
                text = "选择目标 App",
                color = TextPrimary,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = selectedApp?.label ?: "未选择",
                color = TextMuted,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        InputRow {
            BasicTextField(
                value = shortcutName,
                onValueChange = onShortcutNameChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (shortcutName.isBlank()) {
                        Text(
                            text = "填写快捷方式名称",
                            color = TextPlaceholder,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "保存快捷方式",
            onClick = onSave
        )
    }
}

@Composable
private fun SelectAppScreen(
    query: String,
    apps: List<InstalledApp>,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
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
            Text(
                text = "选择目标 App",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF0DDD6))
                .padding(horizontal = 20.dp),
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
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 15.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (query.isBlank()) {
                        Text(
                            text = "搜索应用名称...",
                            color = TextPlaceholder,
                            fontSize = 15.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppRow(app = app, onClick = { onSelectApp(app) })
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
            .clip(RoundedCornerShape(20.dp))
            .background(CardSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconBox(packageName = app.packageName)
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = app.label,
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
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
            .aspectRatio(1.08f)
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
                fontWeight = FontWeight.SemiBold
            )
            leading()
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(40.dp)
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
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        AppIconBox(
            packageName = shortcut.packageName,
            modifier = Modifier.size(28.dp),
            container = Color.Transparent,
            iconSize = 28.dp
        )
    }
}

@Composable
private fun AppIconBox(
    packageName: String,
    modifier: Modifier = Modifier.size(40.dp),
    container: Color = BgSoft,
    iconSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(packageName).toImageBitmap()
        }.getOrNull()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
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
        .height(64.dp)
        .clip(RoundedCornerShape(26.dp))
        .background(Color(0xFFF0DDD6))
        .padding(horizontal = 24.dp)

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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x2E000000),
                spotColor = Color(0x2E000000)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(TextPrimary)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            }
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
    size: androidx.compose.ui.unit.Dp = 48.dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                ambientColor = Color(0x14000000),
                spotColor = Color(0x14000000)
            )
            .clip(CircleShape)
            .background(Card)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(if (size <= 44.dp) 20.dp else 22.dp)
        }
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
