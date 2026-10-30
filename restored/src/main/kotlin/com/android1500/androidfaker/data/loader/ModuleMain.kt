package com.android1500.androidfaker.data.loader

import android.app.Application
import android.content.Context
import android.os.Build
import com.android1500.androidfaker.data.logger.LoggerProxy
import com.android1500.androidfaker.data.xposed.HookEntry
import com.android1500.androidfaker.data.xposed.base.HookLogSink
import com.androidfaker.data.repository.util.HookConfig
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.serialization.json.Json

/** Direct-source replacement for the original native/in-memory DEX packaging. */
class ModuleMain : XposedModule() {
    private val attached = AtomicBoolean(false)
    private var entry: IHookEntry? = null
    private var logger: LoggerProxy? = null
    internal var createLogger: (String) -> LoggerProxy = { LoggerProxy(it) }
    internal var createEntry: (HookLogSink) -> IHookEntry = { HookEntry(it) }

    override fun onPackageLoaded(param: PackageLoadedParam) {
        val packageName = param.packageName
        if (Build.VERSION.SDK_INT < 29 || !param.isFirstPackage || packageName in EXCLUDED ||
            packageName == moduleApplicationInfo.packageName) return
        val loader = param.defaultClassLoader
        try {
            hook(Application::class.java.getDeclaredMethod("attach", Context::class.java)).intercept { chain ->
                val original = chain.proceed()
                if (attached.compareAndSet(false, true)) {
                    val context = chain.thisObject as? Context
                    if (context == null) log(4, TAG, "[$packageName] Context null at Application.attach")
                    else try { initialize(context, loader, packageName) }
                    catch (error: Throwable) { log(6, TAG, "init failed for $packageName", error) }
                }
                original
            }
        } catch (error: Throwable) {
            log(6, TAG, "hook Application.attach failed: ${error.message}")
        }
    }

    private fun initialize(context: Context, loader: ClassLoader, packageName: String) {
        val encoded = getRemotePreferences("hooks").getString("hooks", null)
        if (encoded.isNullOrEmpty()) { log(4, TAG, "no configs JSON for $packageName"); return }
        val configs = runCatching { json.decodeFromString<Map<String, HookConfig>>(encoded) }.getOrDefault(emptyMap())
        if (configs.isEmpty()) { log(4, TAG, "empty configs map for $packageName"); return }
        val config = configs[packageName] ?: return
        if (!config.isAvailable()) { log(4, TAG, "config not available for $packageName"); return }
        // The recovered hook classes are compiled into this APK, so no encrypted DEX,
        // signing-certificate-derived initialization, JNI library or loader is needed.
        val sink = logger ?: createLogger(moduleApplicationInfo.packageName).also { logger = it }
        val hooks = entry ?: createEntry(sink).also { entry = it }
        try { sink.init(context) } catch (error: Throwable) {
            log(5, TAG, "LoggerProxy init failed: ${error.message}")
        }
        hooks.hook(XposedHookBridge(this), loader, config)
        log(4, TAG, "hooks registered for $packageName")
    }

    private companion object {
        const val TAG = "ModuleMain"
        val EXCLUDED = setOf("com.android1500.androidfaker", "android", "com.android.systemui")
        val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }
    }
}
