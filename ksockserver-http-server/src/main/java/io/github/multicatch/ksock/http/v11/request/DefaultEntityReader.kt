package io.github.multicatch.ksock.http.v11.request

import io.github.multicatch.ksock.http.request.EntityReader

class DefaultEntityReader : EntityReader {
    override fun read(headers: Map<String, String>, raw: ByteArray): String? = String(raw)
}