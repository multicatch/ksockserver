package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.tcp.TcpServerConfiguration

fun HttpConfig.alias(url: String, targetUrl: String) = apply {
    this.aliasRules.add(url to targetUrl)
}

fun TcpServerConfiguration<out HttpProtocol>.alias(aliasUrl: String, targetUrl: String) {
    protocol.urls.indexOfFirst { targetUrl.startsWith(it.first) }
            .takeIf { it >= 0 }
            ?.let {
                val (originalUrl, handler) = protocol.urls[it]
                protocol.urls.add(it, aliasUrl to aliasOf(originalUrl, targetUrl, handler))
            }
}

private fun aliasOf(baseUrl: String, targetUrl: String, handler: (HttpRequest) -> HttpResponse): (HttpRequest) -> HttpResponse =
        {
            val request = it.copy(
                    contextPath = baseUrl,
                    resourceUri = targetUrl,
                    resourcePath = targetUrl.drop(baseUrl.length)
            )

            handler(request)
        }
