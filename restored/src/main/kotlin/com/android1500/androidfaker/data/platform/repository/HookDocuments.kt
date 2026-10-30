package com.android1500.androidfaker.data.platform.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.util.Log
import java.io.InputStream
import java.io.OutputStream

/** Narrow test seam around the APK's ContentResolver/DocumentFile operations. */
internal interface HookDocuments {
    fun openInput(uri: Uri): InputStream?
    fun openOutput(uri: Uri): OutputStream?
    fun openDirectory(uri: Uri): Directory

    interface Directory {
        fun find(name: String): Uri?
        fun delete(uri: Uri): Boolean
        fun create(mimeType: String, name: String): Uri?
    }
}

internal class AndroidHookDocuments(private val context: Context) : HookDocuments {
    private val resolver get() = context.contentResolver
    override fun openInput(uri: Uri) = resolver.openInputStream(uri)
    override fun openOutput(uri: Uri) = resolver.openOutputStream(uri)

    override fun openDirectory(uri: Uri): HookDocuments.Directory {
        val id = if (DocumentsContract.isDocumentUri(context, uri)) {
            DocumentsContract.getDocumentId(uri)
        } else {
            DocumentsContract.getTreeDocumentId(uri)
        }
        val document = DocumentsContract.buildDocumentUriUsingTree(uri, id)
        return object : HookDocuments.Directory {
            override fun find(name: String): Uri? {
                val children = mutableListOf<Uri>()
                val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(document, id)
                try {
                    resolver.query(childUri, arrayOf(Document.COLUMN_DOCUMENT_ID), null, null, null)?.use { cursor ->
                        while (cursor.moveToNext()) {
                            children += DocumentsContract.buildDocumentUriUsingTree(document, cursor.getString(0))
                        }
                    }
                } catch (error: Exception) {
                    Log.w("DocumentFile", "Failed query: $error")
                }
                return children.firstOrNull { child ->
                    try {
                        resolver.query(child, arrayOf(Document.COLUMN_DISPLAY_NAME), null, null, null)?.use {
                            it.moveToFirst() && it.getString(0) == name
                        } ?: false
                    } catch (error: Exception) {
                        Log.w("DocumentFile", "Failed query: $error")
                        false
                    }
                }
            }

            override fun delete(uri: Uri): Boolean = try {
                DocumentsContract.deleteDocument(resolver, uri)
            } catch (_: Exception) { false }

            override fun create(mimeType: String, name: String): Uri? = try {
                DocumentsContract.createDocument(resolver, document, mimeType, name)
            } catch (_: Exception) { null }
        }
    }
}
