package com.ekoapp.rxuploadservice.exception

sealed class AmityException(message: String) : Exception(message) {

    object FileNotFoundException : AmityException("file is not found!")
    object MaxUploadSizeExceededException : AmityException("max upload size exceeded!")
    object UnsupportedFileTypeException : AmityException("unsupported file type!")
}