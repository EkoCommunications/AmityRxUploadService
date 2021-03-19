package com.ekoapp.rxuploadservice.service

import com.ekoapp.rxuploadservice.FileProperties
import com.ekoapp.rxuploadservice.RxUploadService
import com.ekoapp.rxuploadservice.service.api.MultipartUploadApi
import io.reactivex.subjects.PublishSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private lateinit var retrofit: Retrofit

class MultipartUploadService {

    companion object {


        private val calls = mutableMapOf<String, Call<ResponseBody>>()
        private val propertiesSubjects = mutableMapOf<String, PublishSubject<FileProperties>>()

        fun init(baseUrl: String, settings: RxUploadService.Settings, interceptors: List<Interceptor>) {
            val httpClient = OkHttpClient.Builder()
                .also {
                    interceptors.forEach { interceptor ->
                        it.addInterceptor(interceptor)
                    }
                }
                .connectTimeout(settings.connectTimeOutMillis, TimeUnit.MILLISECONDS)
                .readTimeout(settings.readTimeOutMillis, TimeUnit.MILLISECONDS)
                .writeTimeout(settings.writeTimeOutMillis, TimeUnit.MILLISECONDS)
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

        fun onRequest(call: Call<ResponseBody>, id: String?) {
            id?.let {
                propertiesSubjects[it] = PublishSubject.create()
                calls[it] = call
            }
        }

        fun onFailure(id: String?) {
            id?.let {
                propertiesSubjects.remove(it)
                calls.remove(it)
            }
        }

        fun onResponse(id: String?) {
            id?.let {
                propertiesSubjects.remove(it)
                calls.remove(it)
            }
        }

        fun properties(id: String?): PublishSubject<FileProperties>? {
            return propertiesSubjects[id]
        }

        fun cancel(id: String) {
            calls.remove(id)?.cancel()
        }
    }
}