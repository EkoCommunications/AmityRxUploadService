package com.ekoapp.rxuploadservice.internal.datastore

interface FileWritingListener {

    fun onWrite(bytesWritten: Long, contentLength: Long)
}