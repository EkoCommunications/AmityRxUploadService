package com.ekoapp.rxuploadservice

import com.ekoapp.rxuploadservice.service.MultipartUploadService
import io.reactivex.Flowable
import okhttp3.Interceptor


class RxUploadService {

    companion object {

        fun init(baseUrl: String, interceptors: List<Interceptor> = emptyList()) {
            MultipartUploadService.init(baseUrl, interceptors)
        }

        fun properties(id: String): Flowable<FileProperties> {
            return Flowable.never()
        }

        fun cancel(id: String) {

        }
    }
}