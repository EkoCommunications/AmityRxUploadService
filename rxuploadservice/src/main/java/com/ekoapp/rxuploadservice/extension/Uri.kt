package com.ekoapp.rxuploadservice.extension

import android.net.Uri
import com.ekoapp.rxuploadservice.model.FileProperties
import io.reactivex.Flowable

fun Uri.upload(id: String? = null, url: String): Flowable<FileProperties> {
    return Flowable.never()
}