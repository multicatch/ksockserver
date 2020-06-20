package io.github.multicatch.ksock.http.request

import java.io.BufferedReader

interface HeaderReader {
    fun read(httpInfo: HttpInfo, bufferedReader: BufferedReader): Map<String, String>?
}

data class HttpInfo(
        val method: String,
        val resource: String,
        val version: String?,
        val remoteAddress: String
)