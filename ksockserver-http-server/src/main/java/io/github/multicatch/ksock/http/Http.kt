package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.response.ResponseWriter
import io.github.multicatch.ksock.http.v11.request.DefaultEntityReader
import io.github.multicatch.ksock.http.v11.request.DefaultHeaderReader
import io.github.multicatch.ksock.http.v11.response.DefaultResponseWriter
import java.net.Socket

object Http : HttpProtocol {
    override val urls: MutableList<Pair<String, (HttpRequest) -> HttpResponse>> = mutableListOf()
    override val entityReaders: MutableList<EntityReader> = mutableListOf()
    override val headerReaders: MutableList<HeaderReader> = mutableListOf()
    override val responseWriters: MutableList<ResponseWriter> = mutableListOf()

    override fun process(connection: Socket) {
        val request = connection.getInputStream()
                .readRequest(connection.inetAddress.hostAddress, headerReaders.toList(), entityReaders.toList())
        println(request)

        val (baseUrl, handler) = request.resourceUri.handler()
        val requestWithContext = request.copy(
                contextPath = baseUrl,
                resourcePath = request.resourceUri.drop(baseUrl.length)
        )

        val response = handler(requestWithContext)

        val result = responseWriters
                .toList()
                .fold(null as ByteArray?) { result, writer ->
                    result ?: writer.write(request, response)
                } ?: error("Cannot write response")

        with(connection.getOutputStream()) {
            write(result)
            flush()
        }
    }

    private fun String.handler() = urls.toList()
            .find { (baseUrl, _) ->
                this.startsWith(baseUrl) || "$this/".startsWith(baseUrl)
            }
            ?: this to { _ -> DEFAULT_RESPONSE }
}

private val DEFAULT_RESPONSE = PlaintextHttpResponse(
        status = StandardHttpStatus.NOT_FOUND,
        originalHeaders = mapOf(
                "Content-Type" to "text/plain"
        ),
        stringEntity = ""
)