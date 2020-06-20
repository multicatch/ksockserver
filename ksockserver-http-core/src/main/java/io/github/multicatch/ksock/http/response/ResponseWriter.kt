package io.github.multicatch.ksock.http.response

import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse

interface ResponseWriter {
    fun write(request: HttpRequest, response: HttpResponse): ByteArray?
}

fun Map<String, String>.toStringHeaders() = map { (name, value) -> "$name: $value" }
        .joinToString("\r\n")