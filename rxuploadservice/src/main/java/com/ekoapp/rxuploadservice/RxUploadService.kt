package com.ekoapp.rxuploadservice

import com.ekoapp.rxuploadservice.service.MultipartUploadService
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import okhttp3.Interceptor

class RxUploadService {

    companion object {

        fun init(baseUrl: String, interceptors: List<Interceptor> = emptyList()) {
            MultipartUploadService.init(baseUrl, interceptors)
        }

        fun properties(id: String): Flowable<FileProperties> {
            return MultipartUploadService.properties(id)?.toFlowable(BackpressureStrategy.BUFFER)
                ?: run { Flowable.never<FileProperties>() }
        }

        fun cancel(id: String) {
            MultipartUploadService.cancel(id)
        }
    }
}