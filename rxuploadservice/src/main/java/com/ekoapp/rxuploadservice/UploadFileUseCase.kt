package com.ekoapp.rxuploadservice

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.internal.repository.FileRepository
import io.reactivex.Flowable

class UploadFileUseCase {

    fun upload(
        context: Context,
        uri: Uri,
        path: String,
        headers: Map<String, Any> = emptyMap(),
        params: Map<String, Any> = emptyMap(),
        id: String? = null
    ): Flowable<FileProperties> {
        return FileRepository().upload(context, uri, path, headers, params, id)
    }
}