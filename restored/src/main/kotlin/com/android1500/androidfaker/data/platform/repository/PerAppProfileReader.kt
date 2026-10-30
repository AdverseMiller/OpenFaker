package com.android1500.androidfaker.data.platform.repository

import android.content.Context
import com.android1500.androidfaker.data.platform.util.RootCommandExecutor

/** Reconstructed listProfilesForApp from ik4.smali, with literal shell-path quoting. */
internal class PerAppProfileReader(
    private val dataDirectory: (String) -> String,
    private val currentProfile: suspend (String) -> String?,
    private val commands: RootCommandExecutor,
) {
    @Suppress("DEPRECATION")
    constructor(context: Context, current: CurrentProfileStore, commands: RootCommandExecutor) : this(
        { context.packageManager.getApplicationInfo(it, 0).dataDir },
        current::getCurrentProfileForApp, commands,
    )

    suspend fun listProfilesForApp(packageName: String): List<AppProfileInfo> {
        val base = "${dataDirectory(packageName)}/.data_profiles"
        val current = currentProfile(packageName)
        val exists = commands.execute("if [ -d ${quoteProfilePath(base)} ]; then echo \"exists\"; fi")
        if (!exists.isSuccess || exists.out.none { it.contains("exists") }) return emptyList()
        val listing = commands.execute("ls -1 ${quoteProfilePath(base)}")
        if (!listing.isSuccess) return emptyList()
        return listing.out.filter { it.isNotBlank() && it != ".current" }.map { name ->
            val result = commands.execute("du -sb ${quoteProfilePath("$base/$name")} 2>/dev/null | cut -f1")
            // No trim and no exit-status check here: both match the recovered bytecode.
            val size = result.out.firstOrNull()?.toLongOrNull() ?: 0L
            AppProfileInfo(name, name == current, size)
        }
    }

    suspend fun getGlobalProfiles(enabledApps: Set<String>): List<Profile> =
        GlobalProfileReader(::listProfilesForApp).getGlobalProfiles(enabledApps)

}
