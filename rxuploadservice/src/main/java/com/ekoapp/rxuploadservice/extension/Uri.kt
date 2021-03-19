package com.ekoapp.rxuploadservice.extension

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.FileProperties
import com.ekoapp.rxuploadservice.UploadFileUseCase
import io.reactivex.Flowable

fun Uri.upload(
    context: Context,
    path: String,
    headers: Map<String, Any> = emptyMap(),
    params: Map<String, Any> = emptyMap(),
    id: String? = null
): Flowable<FileProperties> {
    return UploadFileUseCase().upload(context, this, path, headers, params, id)
}