package com.android1500.androidfaker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.androidfaker.ui.platform.component.theme.SettingsTheme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android1500.androidfaker.ui.platform.feature.log.LogScreen
import com.android1500.androidfaker.ui.platform.feature.log.LogViewModel
import com.android1500.androidfaker.ui.platform.feature.apps.AppListScreen
import com.android1500.androidfaker.ui.platform.feature.apps.AppListViewModel
import com.android1500.androidfaker.ui.platform.feature.hook.HookScreen
import com.android1500.androidfaker.ui.platform.feature.settings.AboutScreen
import com.android1500.androidfaker.ui.platform.feature.settings.SettingsScreen
import com.android1500.androidfaker.ui.platform.feature.settings.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as App
        val models = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val model = when (modelClass) {
                    LogViewModel::class.java -> LogViewModel(app.logRepository)
                    AppListViewModel::class.java -> AppListViewModel(app.appRepository, app.settingsRepository,
                        app.hookRepository, app.profileRepository, app)
                    EditorStore::class.java -> EditorStore()
                    SettingsViewModel::class.java -> SettingsViewModel(app.settingsRepository, app)
                    else -> error("Unsupported ViewModel: ${modelClass.name}")
                }
                return modelClass.cast(model)!!
            }
        })
        val editorStore = models[EditorStore::class.java]
        setContent {
            SettingsTheme(app.settingsRepository) {
                RecoveryNavigation(
                    apps = { open -> AppListScreen(models[AppListViewModel::class.java],
                        onOpenApp = { open(it.packageName, it.label) }) },
                    logs = { LogScreen(models[LogViewModel::class.java]) },
                    settings = { about -> SettingsScreen(models[SettingsViewModel::class.java], about) },
                    about = { back -> AboutScreen(back) },
                    editor = { packageName, name, back -> HookScreen(name, editorStore.get(app, packageName), back) },
                    onCloseEditor = editorStore::close,
                )
            }
        }
    }
}
