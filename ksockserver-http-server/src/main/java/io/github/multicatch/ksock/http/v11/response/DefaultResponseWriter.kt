package io.github.multicatch.ksock.http.v11.response

import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.response.ResponseWriter
import io.github.multicatch.ksock.http.response.toStringHeaders

class DefaultResponseWriter : ResponseWriter {
    override fun write(request: HttpRequest, response: HttpResponse): ByteArray? =
            """HTTP/1.1 ${response.status.code} ${response.status.description}${'\r'}
Server: ksockserver${'\r'}
Connection: close${'\r'}
${response.headers.toStringHeaders()}${'\r'}
${'\r'}
""".toByteArray() + response.entity
}