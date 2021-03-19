package com.ekoapp.rxuploadservice.internal.repository

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.FileProperties
import com.ekoapp.rxuploadservice.internal.datastore.FileLocalDataStore
import com.ekoapp.rxuploadservice.internal.datastore.FileRemoteDataStore
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function3

class FileRepository {

    fun upload(
        context: Context,
        uri: Uri,
        action: String,
        headers: Map<String, String>,
        params: Map<String, String>,
        id: String? = null
    ): Flowable<FileProperties> {
        val localDataStore = FileLocalDataStore()
        val remoteDataStore = FileRemoteDataStore()
        return localDataStore.test(context, uri)
            .andThen(Single.zip(localDataStore.getFileName(context, uri),
                localDataStore.getFileSize(context, uri),
                localDataStore.getMimeType(context, uri),
                Function3<String, Long, String, FileProperties> { fileName, fileSize, mimeType ->
                    FileProperties(
                        uri,
                        fileSize,
                        fileName,
                        mimeType
                    )
                })
                .flatMapPublisher { properties ->
                    localDataStore.getFile(context, uri)
                        .flatMapPublisher {
                            remoteDataStore.upload(
                                it,
                                properties,
                                action,
                                headers,
                                params,
                                id
                            )
                        }
                }).mergeWith(localDataStore.clearCache(context))
    }
}