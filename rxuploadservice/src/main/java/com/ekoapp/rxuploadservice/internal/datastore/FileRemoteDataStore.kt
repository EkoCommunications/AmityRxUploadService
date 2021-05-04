package com.ekoapp.rxuploadservice.internal.datastore

import android.util.Log
import com.ekoapp.rxuploadservice.FileProperties
import com.ekoapp.rxuploadservice.service.MultipartUploadService
import com.ekoapp.rxuploadservice.service.api.MultipartUploadApi
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.Flowable
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import kotlin.math.floor
import kotlin.math.min

class FileRemoteDataStore {

    fun upload(
        file: File,
        fileProperties: FileProperties,
        path: String,
        headers: Map<String, Any>,
        params: Map<String, Any>,
        id: String? = null
    ): Flowable<FileProperties> {
        Log.e("testtest", "upload2")
        return try {
            Log.e("testtest", "try")
            Flowable.fromPublisher<FileProperties> {
                Log.e("testtest", "fromPublisher")
                val mediaType = fileProperties.mimeType.toMediaType()
                val requestBody = file
                    .asRequestBody(mediaType)
                    .asProgressRequestBody(object :
                        FileWritingListener {
                        override fun onWrite(bytesWritten: Long, contentLength: Long) {
                            Log.e("testtest", "onWrite")
                            val progress =
                                min(
                                    floor(bytesWritten.toDouble() / contentLength.toDouble() * 100.toDouble()).toInt(),
                                    99
                                )
                            Log.e("testtest", "progress:$progress")
                            it.onNext(fileProperties.apply {
                                this.bytesWritten = bytesWritten
                                this.contentLength = contentLength
                                this.progress = progress
                            })
                            Log.e("testtest", "onNext")
                            MultipartUploadService.properties(id)?.onNext(fileProperties.apply {
                                this.bytesWritten = bytesWritten
                                this.contentLength = contentLength
                                this.progress = progress
                            })
                            Log.e("testtest", "onNext")
                        }
                    })
                Log.e("testtest", "requestBody")
                val multipartBody = MultipartBody.Part.createFormData(
                    "file",
                    fileProperties.fileName,
                    requestBody
                )
                Log.e("testtest", "multipartBody")
                val multipartUploadApi: MultipartUploadApi = MultipartUploadService.getUploadApi()
                Log.e("testtest", "MultipartUploadApi:$multipartUploadApi")
                val call = multipartUploadApi.upload(
                    path,
                    headers,
                    multipartBody,
                    params.mapValues { param -> param.value.toString().toRequestBody() })
                Log.e("testtest", "call")
                MultipartUploadService.onRequest(call, id)
                Log.e("testtest", "onRequest")
                call.enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e("testtest", "onFailure:$t")
                        it.onError(t)
                        it.onComplete()
                        MultipartUploadService.onFailure(id)
                    }

                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        Log.e("testtest", "onResponse")
                        response.errorBody()?.let { error ->
                            Log.e("testtest", "errorBody!=null")
                            it.onError(Exception(JsonObject().apply {
                                addProperty("errorCode", response.code())
                                addProperty("errorBody", error.string())
                            }.toString()))
                            Log.e("testtest", "onError")
                        } ?: run {
                            Log.e("testtest", "errorBody==null")
                            it.onNext(fileProperties.apply {
                                response.body()?.string().let { jsonString ->
                                    this.responseBody = JsonParser.parseString(jsonString)
                                    this.progress = 100
                                }
                            })
                            Log.e("testtest", "onNext")
                        }

                        it.onComplete()
                        Log.e("testtest", "onComplete")
                        MultipartUploadService.onResponse(id)
                    }
                })
                Log.e("testtest", "enqueue")
            }
        } catch (e: Exception) {
            Log.e("testtest", "catch:" + e.message)
            Flowable.empty()
        }
    }

    companion object {

        fun RequestBody.asProgressRequestBody(listener: FileWritingListener): RequestBody {

            var bytesWritten: Long = 0

            return object : RequestBody() {
                override fun contentType(): MediaType? {
                    return this@asProgressRequestBody.contentType()
                }

                override fun contentLength(): Long {
                    return this@asProgressRequestBody.contentLength()
                }

                override fun writeTo(sink: BufferedSink) {
                    val forwardingSink = object : ForwardingSink(sink) {
                        override fun write(source: Buffer, byteCount: Long) {
                            super.write(source, byteCount)
                            bytesWritten += byteCount
                            listener.onWrite(bytesWritten, contentLength())
                        }
                    }.buffer()
                    this@asProgressRequestBody.writeTo(forwardingSink)
                    forwardingSink.flush()
                }
            }
        }
    }
}