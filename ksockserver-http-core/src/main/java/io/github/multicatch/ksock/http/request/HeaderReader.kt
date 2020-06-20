package io.github.multicatch.ksock.http.request

interface HeaderReader {
    fun readSingle(httpInfo: HttpInfo, line: String): Pair<String, String>?
}

data class HttpInfo(
        val method: String,
        val resource: String,
        val version: String?,
        val remoteAddress: String
)