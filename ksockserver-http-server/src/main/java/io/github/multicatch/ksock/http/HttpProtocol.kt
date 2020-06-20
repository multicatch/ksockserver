package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.response.ResponseWriter
import io.github.multicatch.ksock.tcp.TcpServerConfiguration
import io.github.multicatch.ksock.tcp.TcpProtocolProcessor

interface HttpProtocol : TcpProtocolProcessor {
    val urls: MutableList<Pair<UrlPattern, (HttpRequest) -> HttpResponse>>
    val headerReaders: MutableList<HeaderReader>
    val entityReaders: MutableList<EntityReader>
    val responseWriters: MutableList<ResponseWriter>
}

fun TcpServerConfiguration<out HttpProtocol>.url(url: UrlPattern, configuration: HttpConfig.() -> Unit) {
    val config = HttpConfig().apply(configuration)
    protocol.urls.add(url to config.handler)

    config.aliasRules.forEach { (aliasUrl, targetUrl) ->
        val target = "${url.basePath}/${targetUrl.trimStart('/')}"

        alias(RelativeUrl(url, aliasUrl) to target)
    }

    protocol.urls.sortByDescending { (url, _) ->
        url.specificity
    }
}

fun TcpServerConfiguration<out HttpProtocol>.withResponseWriter(responseWriter: ResponseWriter) {
    protocol.responseWriters.add(0, responseWriter)
}