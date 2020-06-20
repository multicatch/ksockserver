package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.tcp.TcpServerConfiguration

fun HttpConfig.alias(url: UrlPattern, targetUrl: String) = apply {
    this.aliasRules.add(url to targetUrl)
}

fun TcpServerConfiguration<HttpRequest, ByteArray, out HttpProtocol>.alias(alias: Pair<UrlPattern, String>) = alias.also { (aliasUrl, targetUrl) ->
    protocol.urls.indexOfFirst { (url, _) -> url.matches(targetUrl) }
            .takeIf { it >= 0 }
            ?.let {
                val (originalUrl, handler) = protocol.urls[it]
                protocol.urls.add(it, aliasUrl to aliasOf(originalUrl, targetUrl, handler))
            }
}

private fun aliasOf(baseUrl: UrlPattern, targetUrl: String, handler: (HttpRequest) -> HttpResponse): (HttpRequest) -> HttpResponse =
        {
            val request = it.copy(
                    contextPath = baseUrl.basePath,
                    resourceUri = targetUrl,
                    resourcePath = baseUrl.trimBasePath(targetUrl)
            )

            handler(request)
        }
