package com.ekoapp.rxuploadservice.internal.datastore

import com.ekoapp.rxuploadservice.service.FileProperties
import com.ekoapp.rxuploadservice.service.api.MultipartUploadApi
import com.google.gson.JsonPrimitive
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
        action: String,
        headers: Map<String, String>,
        params: Map<String, String>,
        id: String? = null
    ): Flowable<FileProperties> {
        return Flowable.fromPublisher<FileProperties> {
            val mediaType = fileProperties.mimeType.toMediaType()
            val requestBody = file.asRequestBody(mediaType)
            requestBody.asProgressRequestBody(object :
                FileWritingListener {
                override fun onWrite(bytesWritten: Long, contentLength: Long) {
                    it.onNext(fileProperties.apply {
                        this.bytesWritten = bytesWritten
                        this.contentLength = contentLength

                        val progress = floor(bytesWritten.toDouble() / contentLength.toDouble() * 100.toDouble()).toInt()
                        this.progress = min(progress, 100)
                    })
                }
            })

            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                fileProperties.fileName,
                requestBody
            )

            val multipartUploadApi: MultipartUploadApi? = null
            val call = multipartUploadApi
                ?.upload(action, headers, multipartBody, params.mapValues { param -> param.value.toRequestBody() })

            call?.enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    it.onError(t)
                    it.onComplete()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    it.onNext(fileProperties.apply {
                        response.body()?.string().let { jsonString ->
                            this.responseBody = JsonPrimitive(jsonString)
                        }
                    })

                    it.onComplete()
                }
            })
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