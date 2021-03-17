package com.ekoapp.rxuploadservice.service.api

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface MultipartUploadApi {

    @Multipart
    @POST("{action}")
    fun upload(
        action: String,
        @HeaderMap headers: Map<String, String>,
        @Part body: MultipartBody.Part
    ): Call<ResponseBody>
}