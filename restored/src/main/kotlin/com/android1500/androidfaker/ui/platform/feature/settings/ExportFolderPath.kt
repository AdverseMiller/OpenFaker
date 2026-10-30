package com.android1500.androidfaker.ui.platform.feature.settings

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.system.Os
import android.util.Log
import java.io.File
import java.io.IOException

/** Display-only conversion from SettingsViewModel$getPathFromUriString; never replaces the stored URI. */
internal fun resolveExportFolderPath(context: Context, value: String): String {
    if (value.isEmpty()) return value
    return try {
        val uri = Uri.parse(value)
        if (uri.scheme == "file") {
            File(requireNotNull(uri.path)).absolutePath
        } else {
            require(uri.scheme == "content") { "Uri lacks 'content' scheme: $uri" }
            val id = if (DocumentsContract.isDocumentUri(context, uri)) {
                DocumentsContract.getDocumentId(uri)
            } else {
                DocumentsContract.getTreeDocumentId(uri)
            }
            val document = DocumentsContract.buildDocumentUriUsingTree(uri, id)
            context.contentResolver.openFileDescriptor(document, "r")?.use {
                Os.readlink("/proc/self/fd/${it.fd}")
            } ?: throw IOException("Can't open file descriptor for uri: $uri")
        }
    } catch (error: Exception) {
        Log.w("SettingsViewModel", "Can't get path from uri string: $value", error)
        ""
    }
}
