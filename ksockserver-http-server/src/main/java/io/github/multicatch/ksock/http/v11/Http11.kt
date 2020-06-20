package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpProtocol
import io.github.multicatch.ksock.http.v11.request.DefaultEntityReader
import io.github.multicatch.ksock.http.v11.request.DefaultHeaderReader
import io.github.multicatch.ksock.http.v11.response.DefaultResponseWriter
import io.github.multicatch.ksock.tcp.TcpServerConfiguration

fun TcpServerConfiguration<out HttpProtocol>.withHttp11() {
    protocol.headerReaders.add(DefaultHeaderReader())
    protocol.entityReaders.add(DefaultEntityReader())
    protocol.responseWriters.add(DefaultResponseWriter())
}