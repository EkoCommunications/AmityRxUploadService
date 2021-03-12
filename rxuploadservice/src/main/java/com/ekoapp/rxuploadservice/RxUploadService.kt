package com.ekoapp.rxuploadservice

import com.ekoapp.rxuploadservice.model.FileProperties
import io.reactivex.Flowable

class RxUploadService {

    companion object {

        fun getFileProperties(id: String): Flowable<FileProperties> {
            return Flowable.never()
        }

        fun cancel(id: String) {

        }
    }
}