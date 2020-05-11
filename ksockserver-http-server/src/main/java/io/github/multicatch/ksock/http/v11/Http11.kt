package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpProtocol
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.HttpStatus
import java.net.Socket

class Http11 : HttpProtocol {
    private val urls: MutableList<Pair<String, (HttpRequest) -> HttpResponse>> = mutableListOf()

    override fun registerUrl(baseUrl: String, handler: (HttpRequest) -> HttpResponse) {
        urls += baseUrl to handler
    }

    override fun process(connection: Socket) {
        val request = connection.getInputStream()
                .readRequest(connection.inetAddress.hostAddress)
        println(request)

        val (baseUrl, handler) = request.resourceUri.handler()
        val requestWithContext = request.copy(
                contextPath = baseUrl,
                resourcePath = request.resourceUri.drop(baseUrl.length)
        )

        val response = handler(requestWithContext)

        connection.getOutputStream().write(response)
    }

    private fun String.handler() = urls.toList()
            .find { (baseUrl, _) ->
                this.startsWith(baseUrl)
            }
            ?: this to { _ -> DEFAULT_RESPONSE }
}

private val DEFAULT_RESPONSE = HttpResponse(
        status = HttpStatus.NOT_FOUND,
        headers = mapOf(
                "Content-Type" to "text/plain"
        ),
        entity = ""
)