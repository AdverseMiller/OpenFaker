package com.android1500.androidfaker.data.platform.repository

import com.android1500.androidfaker.data.platform.util.RootCommandExecutor

/** switchProfileForApp reconstructed from ik4.smali. No commands run until explicitly called. */
internal class ProfileDataSwitcher(
    private val dataDirectory: (String) -> String,
    private val listChildren: (String) -> List<String>?,
    private val currentProfile: suspend (String) -> String?,
    private val recordCurrent: (String, String) -> Unit,
    private val commands: RootCommandExecutor,
) {
    suspend fun switchProfileForApp(packageName: String, profileName: String) {
        checkName(profileName)
        val data = dataDirectory(packageName)
        require(packageName.matches(Regex("[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*")))
        require(data == "/data/data/$packageName" ||
            data.matches(Regex("/data/user(?:_de)?/[0-9]+/${Regex.escape(packageName)}"))) {
            "Unexpected app data directory"
        }
        val base = "$data/.data_profiles"
        val target = "$base/$profileName"
        val exists = commands.execute("if [ -d ${quoteProfilePath(target)} ]; then echo \"exists\"; fi")
        if (!exists.isSuccess || exists.out.none { it.contains("exists") }) {
            commands.execute("mkdir -p ${quoteProfilePath(target)} && chmod 775 ${quoteProfilePath(target)}")
        }
        val current = currentProfile(packageName)
        if (current == profileName) return
        if (current != null) checkName(current)
        val liveFiles = listChildren(data).orEmpty().filter { it != "lib" && it != ".data_profiles" }
        liveFiles.forEach(::checkChild)
        if (current != null) {
            val old = "$base/$current"
            commands.execute("mkdir -p ${quoteProfilePath(old)}")
            for (file in liveFiles) {
                commands.execute("mv -f ${quoteProfilePath("$data/$file")} ${quoteProfilePath("$old/")}")
            }
        } else {
            // Original first-switch branch discards live files when no current profile exists.
            for (file in liveFiles) commands.execute("rm -rf ${quoteProfilePath("$data/$file")}")
        }
        val newFiles = listChildren(target).orEmpty()
        newFiles.forEach(::checkChild)
        for (file in newFiles) {
            commands.execute("mv -f ${quoteProfilePath("$target/$file")} ${quoteProfilePath("$data/")}")
        }
        val stat = commands.execute("stat -c '%u:%g' ${quoteProfilePath(data)}")
        if (stat.isSuccess && stat.out.isNotEmpty()) {
            val owner = stat.out.first().trim()
            // Prevent command interpolation from unexpected stat output.
            require(owner.matches(Regex("[0-9]+:[0-9]+"))) { "Invalid app data ownership" }
            commands.execute("chown -R $owner ${quoteProfilePath(data)}")
        }
        commands.execute("restorecon -RF ${quoteProfilePath(data)}")
        commands.execute("rm -f ${quoteProfilePath("$base/.current")}")
        commands.execute("ln -s ${quoteProfilePath(profileName)} ${quoteProfilePath("$base/.current")}")
        // APK ignores nonzero command results here, including link creation, and records the new name.
        recordCurrent(packageName, profileName)
    }

    private fun checkName(value: String) {
        checkChild(value)
        require(value.isNotBlank() && value != ".current") { "Invalid profile name" }
    }

    private fun checkChild(value: String) {
        require(value.isNotEmpty() && value != "." && value != ".." &&
            value.none { it == '/' || it == '\u0000' || it == '\n' || it == '\r' }) { "Invalid child name" }
    }
}
