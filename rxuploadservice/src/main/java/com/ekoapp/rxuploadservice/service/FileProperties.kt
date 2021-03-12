package com.ekoapp.rxuploadservice.service

import android.net.Uri
import com.google.gson.JsonElement
import com.google.gson.JsonNull

data class FileProperties(
    val uri: Uri,
    val fileSize: Int,
    val fileName: String,
    val mimeType: String,
    val bytesWritten: Long = 0,
    val contentLength: Long = 0,
    val progress: Int = 0,
    val responseBody: JsonElement = JsonNull.INSTANCE
)