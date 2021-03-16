package com.ekoapp.rxuploadservice.internal.datastore

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.webkit.MimeTypeMap
import io.reactivex.Completable
import io.reactivex.Single
import java.io.File
import java.util.*

class FileLocalDataStore {

    private val cacheDirectory: String = "AMITY_RX_UPLOAD_SERVICE_CACHE"

    private fun isFile(uri: Uri): Boolean {
        Log.e("testtest", "scheme:${uri.scheme}")
        return uri.scheme == null || uri.scheme == ContentResolver.SCHEME_FILE
    }

    private fun isContent(uri: Uri): Boolean {
        return uri.scheme == ContentResolver.SCHEME_CONTENT
    }

    private fun isDocument(context: Context, uri: Uri): Boolean {
        return DocumentsContract.isDocumentUri(context, uri)
    }

    private fun mimeTypeFromUri(context: Context, uri: Uri): String? {
        if (isFile(uri)) {
            return MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(
                    MimeTypeMap.getFileExtensionFromUrl(uri.toString())
                        .toLowerCase(Locale.getDefault())
                )
        }

        return context.contentResolver.getType(uri)
    }

    private fun fileNameFromUri(context: Context, uri: Uri): String? {
        if (isFile(uri)) {
            return uri.path?.let { File(it).name }
        }

        context.contentResolver.query(
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

    private fun fileSizeFromUri(context: Context, uri: Uri): Long? {
        if (isFile(uri)) {
            return uri.path?.let { File(it).length() }
        }

        context.contentResolver.query(
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

    private fun byteArrayFromUri(context: Context, uri: Uri) {
        Log.e("testtest", "uri:${uri.path}")

        if (isFile(uri)) {
            val readBytes = uri.path?.let { File(it).readBytes() }
            Log.e("testtest", "readBytes:${readBytes?.size}")
        }

        val readBytes = context.contentResolver.openInputStream(uri)?.readBytes()
        Log.e("testtest", "readBytes:${readBytes?.size}")
    }

    private fun fileFromUri(context: Context, uri: Uri) {
        Log.e("testtest", "uri:${uri.path}")

        if (isFile(uri)) {
            val file = uri.path?.let { File(it) }
            Log.e(
                "testtest",
                String.format(
                    "path:%s absolutePath:%s exists:%s",
                    file?.path,
                    file?.absolutePath,
                    file?.exists()
                )
            )
        }

        val file = context.contentResolver.openInputStream(uri)
            ?.use {
                val directory = File(context.cacheDir, cacheDirectory)
                directory.mkdirs()
                val output = File(directory, UUID.randomUUID().toString())
                it.copyTo(output.outputStream())
                output
            }

        Log.e(
            "testtest",
            String.format(
                "path:%s absolutePath:%s exists:%s",
                file?.path,
                file?.absolutePath,
                file?.exists()
            )
        )
    }

    fun getMimeType(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            mimeTypeFromUri(context, uri)
                ?.let { mimeType ->
                    it.onNext(mimeType)
                    it.onComplete()
                }
        }
    }

    fun getFileName(context: Context, uri: Uri): Single<String> {
        return Single.fromPublisher {
            fileNameFromUri(context, uri)
                ?.let { fileName ->
                    it.onNext(fileName)
                    it.onComplete()
                }
        }
    }

    fun getFileSize(context: Context, uri: Uri): Single<Long> {
        return Single.fromPublisher {
            fileSizeFromUri(context, uri)
                ?.let { fileSize ->
                    it.onNext(fileSize)
                    it.onComplete()
                }
        }
    }

    fun clearCache(context: Context): Completable {
        return Completable.fromAction {
            val directory = File(context.cacheDir, cacheDirectory)
            directory.deleteRecursively()
        }
    }
}