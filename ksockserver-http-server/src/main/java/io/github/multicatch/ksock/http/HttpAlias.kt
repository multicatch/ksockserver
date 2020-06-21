package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.RequestHandler
import io.github.multicatch.ksock.http.request.RequestHandlerFactory
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

private fun aliasOf(baseUrl: UrlPattern, targetUrl: String, handler: RequestHandlerFactory): RequestHandlerFactory =
        AliasedRequestHandler(baseUrl, targetUrl, handler)

class AliasedRequestHandler(
        private val baseUrl: UrlPattern,
        private val targetUrl: String,
        private val requestHandlerFactory: RequestHandlerFactory
) : RequestHandler, RequestHandlerFactory {

    private lateinit var requestHandler: RequestHandler
    override fun handle(request: HttpRequest): HttpResponse {
        val aliasedRequest = request.copy(
                contextPath = baseUrl.basePath,
                resourceUri = targetUrl,
                resourcePath = baseUrl.trimBasePath(targetUrl)
        )
        requestHandler = requestHandlerFactory.create()

        return requestHandler.handle(aliasedRequest)
    }

    override fun interrupt() {
        requestHandler.interrupt()
    }

    override fun resume(): HttpResponse {
        return requestHandler.resume()
    }

    override fun create(): RequestHandler {
        return AliasedRequestHandler(baseUrl, targetUrl, requestHandlerFactory)
    }
}