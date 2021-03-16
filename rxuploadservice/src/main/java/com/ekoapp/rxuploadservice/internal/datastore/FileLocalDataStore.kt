package com.ekoapp.rxuploadservice.internal.datastore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import io.reactivex.Single
import java.io.File

class FileLocalDataStore {

    private fun isFile(uri: Uri): Boolean {
        return uri.scheme == null || uri.scheme == ContentResolver.SCHEME_FILE
    }

    private fun getFileMimeType(contentResolver: ContentResolver, uri: Uri): String? {
        if (isFile(uri)) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase())
        }

        return contentResolver.getType(uri)
    }

    private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
        if (isFile(uri)) {
            return uri.path?.let { File(it).name }
        }

        contentResolver.query(
            uri,
            null,
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }

        return null
    }

    private fun getFileSize(contentResolver: ContentResolver, uri: Uri): Long? {
        if (isFile(uri)) {
            return uri.path?.let { File(it).length() }
        }

        contentResolver.query(
            uri,
            null,
            null,
            null,
            null,
            null
        )?.use {
            if (it.moveToFirst()) {
                return it.getLong(it.getColumnIndex(OpenableColumns.SIZE))
            }
        }

        return null
    }

    private fun getPathFromUri(context: Context, uri: Uri): String? {
        return null
    }

    fun getFileMimeType(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            getFileMimeType(context.contentResolver, uri)
                ?.let { mimeType ->
                    it.onNext(mimeType)
                    it.onComplete()
                }
        }
    }

    fun getFileName(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            getFileName(context.contentResolver, uri)
                ?.let { fileName ->
                    it.onNext(fileName)
                    it.onComplete()
                }
        }
    }

    fun getFileSize(context: Context, uri: Uri): Single<Long> {
        return Single.fromPublisher {
            getFileSize(context.contentResolver, uri)
                ?.let { fileSize ->
                    it.onNext(fileSize)
                    it.onComplete()
                }
        }
    }

    fun getFilePath(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            getPathFromUri(context, uri)
                ?.let { path ->
                    it.onNext(path)
                    it.onComplete()
                }
        }
    }
}