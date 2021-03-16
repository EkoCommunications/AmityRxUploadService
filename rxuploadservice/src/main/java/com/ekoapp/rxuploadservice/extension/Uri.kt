package com.ekoapp.rxuploadservice.extension

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.internal.usecase.UploadFileUseCase
import com.ekoapp.rxuploadservice.service.FileProperties
import io.reactivex.Flowable

fun Uri.upload(
    context: Context,
    action: String,
    headers: Map<String, String> = emptyMap(),
    id: String? = null
): Flowable<FileProperties> {
    return UploadFileUseCase().upload(context, action, headers, id)
}