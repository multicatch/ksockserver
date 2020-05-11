package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.tcp.TcpServerConfiguration
import io.github.multicatch.ksock.tcp.TcpProtocolProcessor

interface HttpProtocol : TcpProtocolProcessor {
    fun registerUrl(baseUrl: String, handler: (HttpRequest) -> HttpResponse)
}

fun TcpServerConfiguration<out HttpProtocol>.url(baseUrl: String, configuration: HttpConfig.() -> Unit) {
    val config = HttpConfig().apply(configuration)
    protocol.registerUrl(baseUrl, config.handler)
}