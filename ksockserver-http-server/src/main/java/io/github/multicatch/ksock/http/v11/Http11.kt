package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpProtocol
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.exception.withExceptionMapper
import io.github.multicatch.ksock.http.exceptions.DefaultHttpExceptionMapper
import io.github.multicatch.ksock.http.v11.request.DefaultEntityReader
import io.github.multicatch.ksock.http.v11.request.DefaultHeaderReader
import io.github.multicatch.ksock.http.v11.response.DefaultResponseWriter
import io.github.multicatch.ksock.tcp.TcpServerConfiguration

fun TcpServerConfiguration<HttpRequest, ByteArray, out HttpProtocol>.withHttp11() {
    protocol.headerReaders.add(DefaultHeaderReader())
    protocol.entityReaders.add(DefaultEntityReader())
    protocol.responseWriters.add(DefaultResponseWriter())
    withExceptionMapper(DefaultHttpExceptionMapper())
}