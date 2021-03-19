package com.ekoapp.rxuploadservice.service.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface MultipartUploadApi {

    // https://github.com/square/retrofit/issues/3275
    @JvmSuppressWildcards
    @Multipart
    @POST("/file/{action}")
    fun upload(
        @Path("action") action: String,
        @HeaderMap headers: Map<String, String>,
        @Part body: MultipartBody.Part,
        @PartMap params: Map<String, RequestBody>
    ): Call<ResponseBody>
}