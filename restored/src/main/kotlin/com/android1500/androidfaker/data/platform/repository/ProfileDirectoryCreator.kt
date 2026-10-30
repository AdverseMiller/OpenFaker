package com.android1500.androidfaker.data.platform.repository

import android.content.Context
import com.android1500.androidfaker.data.platform.util.RootCommandExecutor

/** Recovered createProfileForApp; does not copy or switch live app data. */
internal class ProfileDirectoryCreator(
    private val dataDirectory: (String) -> String,
    private val currentProfile: suspend (String) -> String?,
    private val recordCurrent: (String, String) -> Unit,
    private val commands: RootCommandExecutor,
) {
    @Suppress("DEPRECATION")
    constructor(context: Context, current: CurrentProfileStore, commands: RootCommandExecutor) : this(
        { context.packageManager.getApplicationInfo(it, 0).dataDir },
        current::getCurrentProfileForApp, current::record, commands,
    )

    suspend fun createProfileForApp(packageName: String, profileName: String) {
        // Reconstruction hardening: a profile must be one child directory, not a path.
        require(profileName.isNotBlank() && profileName !in setOf(".", "..", ".current") &&
            '/' !in profileName && '\u0000' !in profileName && '\n' !in profileName && '\r' !in profileName) {
            "Invalid profile name"
        }
        val base = "${dataDirectory(packageName)}/.data_profiles"
        val target = "$base/$profileName"
        val result = commands.execute(listOf(
            "mkdir -p ${quoteProfilePath(base)}",
            "mkdir -p ${quoteProfilePath(target)}",
            "chmod 775 ${quoteProfilePath(target)}",
        ).joinToString("\n"))
        if (!result.isSuccess) throw RuntimeException(
            "Failed to create profile directory for $packageName: ${result.err.joinToString()}")
        if (currentProfile(packageName) == null) {
            val link = commands.execute("ln -s ${quoteProfilePath(profileName)} ${quoteProfilePath("$base/.current")}")
            if (link.isSuccess) recordCurrent(packageName, profileName)
        }
    }
}

/** Treat path text literally, including quotes and shell substitutions. */
internal fun quoteProfilePath(value: String): String {
    require('\u0000' !in value) { "Invalid path" }
    return "'" + value.replace("'", "'\"'\"'") + "'"
}
