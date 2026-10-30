package com.android1500.androidfaker.data.platform.util

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Descriptive reconstructed names; original ShellResult fields are preserved. */
data class ShellResult(val out: List<String>, val err: List<String>, val code: Int, val isSuccess: Boolean)

fun interface RootCommandExecutor {
    suspend fun execute(command: String): ShellResult
}

class LibsuRootCommandExecutor(
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : RootCommandExecutor {
    override suspend fun execute(command: String): ShellResult = withContext(ioDispatcher) {
        val hasRoot = Shell.isAppGrantedRoot() ?: Shell.getShell().isRoot
        if (!hasRoot) throw RuntimeException("Root unavailable")
        val result = Shell.cmd(command).exec()
        ShellResult(result.out, result.err, result.code, result.code == 0)
    }
}
