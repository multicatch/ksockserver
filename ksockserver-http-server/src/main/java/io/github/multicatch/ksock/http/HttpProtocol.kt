package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.tcp.TcpServerConfiguration
import io.github.multicatch.ksock.tcp.TcpProtocolProcessor

interface HttpProtocol : TcpProtocolProcessor {
    val urls: MutableMap<String, (HttpRequest) -> HttpResponse>
}

fun TcpServerConfiguration<out HttpProtocol>.url(path: String, mapping: () -> (HttpRequest) -> HttpResponse) {
    protocol.urls[path] = mapping()
}
