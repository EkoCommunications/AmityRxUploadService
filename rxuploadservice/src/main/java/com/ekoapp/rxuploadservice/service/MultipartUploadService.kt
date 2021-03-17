package com.ekoapp.rxuploadservice.service

import com.ekoapp.rxuploadservice.service.api.MultipartUploadApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

private lateinit var retrofit: Retrofit

class MultipartUploadService {

    companion object {

        fun init(baseUrl: String, interceptors: List<Interceptor>) {
            val httpClient = OkHttpClient.Builder()
                .also {
                    interceptors.forEach { interceptor ->
                        it.addInterceptor(interceptor)
                    }
                }
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        }

        fun getUploadApi(): MultipartUploadApi {
            return retrofit.create(MultipartUploadApi::class.java)
        }
    }
}