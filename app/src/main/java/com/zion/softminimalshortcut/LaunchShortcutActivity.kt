package com.zion.softminimalshortcut

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.zion.softminimalshortcut.shortcut.ShortcutUtils

class LaunchShortcutActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val packageName = intent.getStringExtra(ShortcutUtils.EXTRA_PACKAGE_NAME)
        val activityName = intent.getStringExtra(ShortcutUtils.EXTRA_ACTIVITY_NAME)

        if (packageName.isNullOrBlank()) {
            finish()
            return
        }

        val launched = ShortcutUtils.launchTargetApp(
            context = this,
            packageName = packageName,
            activityName = activityName
        )

        if (!launched) {
            Toast.makeText(this, "目标 App 已不存在或无法启动", Toast.LENGTH_SHORT).show()
        }

        finish()
        overridePendingTransition(0, 0)
    }
}
