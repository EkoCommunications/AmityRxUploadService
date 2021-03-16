package com.ekoapp.rxuploadservice.internal.usecase

import android.content.Context
import com.ekoapp.rxuploadservice.internal.repository.FileRepository
import com.ekoapp.rxuploadservice.service.FileProperties
import io.reactivex.Flowable

class UploadFileUseCase {

    fun upload(
        context: Context,
        action: String,
        headers: Map<String, String> = emptyMap(),
        id: String? = null
    ): Flowable<FileProperties> {
        return FileRepository().upload(context, action, headers, id)
    }
}