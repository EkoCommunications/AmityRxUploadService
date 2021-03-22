package com.ekoapp.rxuploadservice.internal.repository

import android.content.Context
import android.net.Uri
import android.os.LimitExceededException
import com.ekoapp.rxuploadservice.FileProperties
import com.ekoapp.rxuploadservice.internal.datastore.FileLocalDataStore
import com.ekoapp.rxuploadservice.internal.datastore.FileRemoteDataStore
import com.ekoapp.rxuploadservice.service.MultipartUploadService
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function3

class FileRepository {

    fun upload(
        context: Context,
        uri: Uri,
        path: String,
        headers: Map<String, Any>,
        params: Map<String, Any>,
        id: String? = null
    ): Flowable<FileProperties> {
        val localDataStore = FileLocalDataStore()
        val remoteDataStore = FileRemoteDataStore()
        return localDataStore.test(context, uri)
            .andThen(Single.zip(localDataStore.getFileName(context, uri),
                localDataStore.getFileSize(context, uri)
                    .flatMap {
                        return@flatMap when (it > MultipartUploadService.getSettings().maximumFileSize) {
                            true -> Single.error<Long>(
                                LimitExceededException(
                                    String.format(
                                        "a file size is %s, the maximum file size is %s",
                                        it,
                                        MultipartUploadService.getSettings().maximumFileSize
                                    )
                                )
                            )
                            false -> Single.just(it)
                        }
                    },
                localDataStore.getMimeType(context, uri)
                    .flatMap {
                        val supportedMimeTypes = MultipartUploadService.getSettings().supportedMimeTypes
                        return@flatMap when (supportedMimeTypes.isNotEmpty() && !supportedMimeTypes.contains(it)) {
                            true -> Single.error<String>(
                                UnsupportedOperationException(
                                    String.format(
                                        "the library doesn't support '%s' mime type",
                                        it
                                    )
                                )
                            )
                            false -> Single.just(it)
                        }
                    },
                Function3<String, Long, String, FileProperties> { fileName, fileSize, mimeType ->
                    FileProperties(
                        uri,
                        fileSize,
                        fileName,
                        mimeType
                    )
                })
                .flatMapPublisher { fileProperties ->
                    localDataStore.getFile(context, uri)
                        .flatMapPublisher {
                            remoteDataStore.upload(
                                it,
                                fileProperties,
                                path,
                                headers,
                                params,
                                id
                            )
                        }
                })
            .doOnTerminate { localDataStore.clearCache(context) }
            .doOnCancel { /*do not clear cache here! just because the subscription is cancelled doesn't mean the upload is also cancel (silent upload!)*/ }
            .distinct { it.progress }
    }
}