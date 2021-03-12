package com.ekoapp.rxuploadservice.extension

import android.content.Context
import android.net.Uri
import com.ekoapp.rxuploadservice.model.FileProperties
import io.reactivex.Flowable

fun Uri.upload(context: Context, url: String, id: String? = null): Flowable<FileProperties> {
    return Flowable.never()
}