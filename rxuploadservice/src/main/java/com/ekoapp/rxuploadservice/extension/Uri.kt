package com.ekoapp.rxuploadservice.extension

import android.content.Context
import android.net.Uri
import androidx.annotation.WorkerThread
import com.ekoapp.rxuploadservice.FileProperties
import com.ekoapp.rxuploadservice.UploadFileUseCase
import io.reactivex.Flowable

@WorkerThread
fun Uri.upload(
    context: Context,
    path: String,
    headers: Map<String, Any> = emptyMap(),
    params: Map<String, Any> = emptyMap(),
    id: String? = null,
    multipartDataKey: String = "file"
): Flowable<FileProperties> {
    return UploadFileUseCase().upload(context, this, path, headers, params, id, multipartDataKey)
}