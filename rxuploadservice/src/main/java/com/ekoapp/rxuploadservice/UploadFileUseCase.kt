package com.ekoapp.rxuploadservice

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.internal.repository.FileRepository
import io.reactivex.Flowable

class UploadFileUseCase {

    fun upload(
        context: Context,
        uri: Uri,
        action: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap(),
        id: String? = null
    ): Flowable<FileProperties> {
        return FileRepository().upload(context, uri, action, headers, params, id)
    }
}