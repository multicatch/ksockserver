package io.github.multicatch.ksock.http.request

interface EntityReader {
    fun read(headers: Map<String, String>, raw: ByteArray): String?
}