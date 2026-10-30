package com.android1500.androidfaker.data.platform.repository

import android.content.Context
import android.content.SharedPreferences
import com.android1500.androidfaker.data.platform.util.LibsuRootCommandExecutor
import com.android1500.androidfaker.data.platform.util.RootCommandExecutor
import com.topjohnwu.superuser.io.SuFile

/** Readable composition of the recovered ProfileRepositoryImpl helpers. */
class ProfileRepositoryImpl internal constructor(
    preferences: SharedPreferences,
    private val dataDirectory: (String) -> String,
    listChildren: (String) -> List<String>?,
    private val commands: RootCommandExecutor,
) : ProfileRepository {
    @Suppress("DEPRECATION")
    constructor(context: Context) : this(
        context.getSharedPreferences(CurrentProfileStore.STORAGE_NAME, Context.MODE_PRIVATE),
        { context.packageManager.getApplicationInfo(it, 0).dataDir },
        { SuFile(it).listFiles()?.map { child -> child.name } },
        LibsuRootCommandExecutor(),
    )

    private val current = CurrentProfileStore(preferences, commands)
    private val reader = PerAppProfileReader(dataDirectory, current::getCurrentProfileForApp, commands)
    private val creator = ProfileDirectoryCreator(dataDirectory, current::getCurrentProfileForApp, current::record, commands)
    private val switcher = ProfileDataSwitcher(dataDirectory, listChildren, current::getCurrentProfileForApp, current::record, commands)
    private val enrollment = ProfileEnrollment(creator::createProfileForApp, switcher::switchProfileForApp,
        dataDirectory, { current.record(it, null) }, commands)
    private val global = GlobalProfileActions(creator::createProfileForApp, switcher::switchProfileForApp,
        ::deleteProfileForApp, ::forceStop, current::getActiveProfileName)

    override suspend fun createGlobalProfile(enabledApps: Set<String>, profileName: String) =
        global.createGlobalProfile(enabledApps, profileName)
    override suspend fun deleteGlobalProfile(enabledApps: Set<String>, profileName: String) =
        global.deleteGlobalProfile(enabledApps, profileName)
    override suspend fun switchGlobalProfile(enabledApps: Set<String>, profileName: String) =
        global.switchGlobalProfile(enabledApps, profileName)
    override suspend fun getGlobalProfiles(enabledApps: Set<String>) = reader.getGlobalProfiles(enabledApps)
    override suspend fun onAppEnabled(packageName: String, profileNames: List<String>) =
        enrollment.onAppEnabled(packageName, profileNames)
    override suspend fun onAppDisabled(packageName: String) = enrollment.onAppDisabled(packageName)

    override suspend fun isProfileFeatureAvailable(): Boolean = try {
        val identity = commands.execute("id")
        if (!identity.isSuccess || identity.out.none { it.contains("uid=0") }) false else {
            // The APK executes all three checks before combining their statuses.
            val find = commands.execute("which find")
            val ls = commands.execute("which ls")
            val mkdir = commands.execute("which mkdir")
            find.isSuccess && ls.isSuccess && mkdir.isSuccess
        }
    } catch (_: Exception) { false }

    private suspend fun forceStop(packageName: String) {
        checkPackage(packageName)
        commands.execute("am force-stop $packageName")
    }

    private suspend fun deleteProfileForApp(packageName: String, profileName: String) {
        checkPackage(packageName)
        require(profileName.isNotBlank() && profileName !in setOf(".", "..", ".current") &&
            profileName.none { it == '/' || it == '\u0000' || it == '\n' || it == '\r' }) { "Invalid profile name" }
        val directory = dataDirectory(packageName)
        require(directory == "/data/data/$packageName" ||
            directory.matches(Regex("/data/user(?:_de)?/[0-9]+/${Regex.escape(packageName)}"))) {
            "Unexpected app data directory"
        }
        commands.execute("rm -rf ${quoteProfilePath("$directory/.data_profiles/$profileName")}")
    }

    private fun checkPackage(value: String) {
        require(value.matches(Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*"))) { "Invalid package name" }
    }
}
