package com.ekoapp.rxuploadservice.internal.repository

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.internal.datastore.FileLocalDataStore
import com.ekoapp.rxuploadservice.internal.datastore.FileRemoteDataStore
import com.ekoapp.rxuploadservice.service.FileProperties
import io.reactivex.Flowable

class FileRepository {

    fun upload(
        context: Context,
        uri: Uri,
        action: String,
        headers: Map<String, String> = emptyMap(),
        id: String? = null
    ): Flowable<FileProperties> {
        val localDataStore = FileLocalDataStore()
        val remoteDataStore = FileRemoteDataStore()
        return Flowable.never()
    }
}