package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpProtocol
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.HttpStatus
import java.net.Socket

class Http11 : HttpProtocol {
    override val urls: MutableMap<String, (HttpRequest) -> HttpResponse> = mutableMapOf()

    override fun process(connection: Socket) {
        val request = connection.getInputStream().readRequest(connection.inetAddress.hostAddress)
        println(request)

        val handler = urls.getOrElse(request.resourceUri) {
            urls.entries
                    .find { request.resourceUri.matches(Regex(it.key)) }
                    ?.value
                    ?: { DEFAULT_RESPONSE }
        }

        val response = handler(request)

        connection.getOutputStream().write(response)
    }
}

private val DEFAULT_RESPONSE = HttpResponse(
        status = HttpStatus.NOT_FOUND,
        headers = mapOf(
                "Content-Type" to "text/plain"
        ),
        entity = ""
)