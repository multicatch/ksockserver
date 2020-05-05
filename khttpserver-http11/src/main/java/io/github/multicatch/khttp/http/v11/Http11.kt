package io.github.multicatch.khttp.http.v11

import io.github.multicatch.khttp.http.HttpRequest
import io.github.multicatch.khttp.http.HttpResponse
import io.github.multicatch.khttp.http.HttpStatus
import java.net.Socket

class Http11 : HttpProtocol {
    override val urls: MutableMap<String, (HttpRequest) -> HttpResponse> = mutableMapOf()

    override fun process(connection: Socket) {
        val request = connection.getInputStream().readRequest(connection.inetAddress.hostAddress)
        println(request)

        val handler = urls.getOrDefault(request.resourceUri) {
            DEFAULT_RESPONSE
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