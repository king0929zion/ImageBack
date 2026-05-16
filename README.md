# Soft Minimal Shortcut

这是一个从零搭建的 Android 项目，用来创建“自定义图标 + 自定义名称”的桌面快捷方式，并跳转到用户选择的目标 App。

## 已实现内容

- 按你提供的 HTML 视觉风格还原了三屏界面：
  - 首页
  - 创建图标页
  - 选择目标 App 页
- 首页会显示已创建的快捷方式数量和快捷入口卡片
- 可以读取设备上的可启动应用并支持搜索
- 可以从系统相册选择图片作为快捷方式图标
- 可以自定义快捷方式名称
- 通过 `ShortcutManagerCompat.requestPinShortcut(...)` 请求在桌面创建快捷方式
- 快捷方式点击后会先进入透明中转页，再拉起目标 App
- 项目默认使用透明启动图标，符合“透明图标快捷启动 App”的方向

## 关键文件

- `app/src/main/java/com/zion/softminimalshortcut/MainActivity.kt`
  - Compose UI 与三屏交互
- `app/src/main/java/com/zion/softminimalshortcut/MainViewModel.kt`
  - 页面状态、保存逻辑、快捷方式流程
- `app/src/main/java/com/zion/softminimalshortcut/LaunchShortcutActivity.kt`
  - 桌面快捷方式点击后的透明跳转 Activity
- `app/src/main/java/com/zion/softminimalshortcut/shortcut/ShortcutUtils.kt`
  - 固定快捷方式创建与目标 App 启动
- `app/src/main/java/com/zion/softminimalshortcut/data/AppDiscovery.kt`
  - 已安装可启动 App 列表读取
- `app/src/main/java/com/zion/softminimalshortcut/data/ShortcutStore.kt`
  - 已创建快捷方式配置持久化

## 打开方式

1. 用 Android Studio 打开 `/workspace`
2. 安装 Android SDK 35 与对应 Build Tools
3. 如果你的本地没有现成 Gradle wrapper，可在本机执行一次 `gradle wrapper` 生成 wrapper 文件，或者让 Android Studio 使用本地 Gradle 完成同步
4. 运行 `app` 模块到真机测试

## 测试重点

- 是否能正确列出设备上的 App
- 选择图片后预览是否正常
- 保存后是否弹出桌面固定快捷方式请求
- 新建的桌面快捷方式是否能拉起目标 App
- 使用系统桌面不支持固定快捷方式时，提示是否合理

## 当前说明

当前容器里没有 Android SDK，也无法联网下载 Gradle 发行包，所以我没法在这里完成真实编译和 APK 产物验证；项目代码与结构已经按可落地方式搭好，建议你在本地 Android Studio 打开后直接同步并运行.
