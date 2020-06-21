package io.github.multicatch.ksock.handlers.http.proxy

import io.github.multicatch.ksock.http.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

fun HttpConfig.proxy(targetAddress: String) = apply {
    this.handler = ProxyHandlerFactory(targetAddress)
}

fun socketOf(host: String): Socket {
    val hostAddress = host.substringBefore(":")
    val port = host.substringAfter(":", "80").toInt()

    return if (host.contains("https")) {
        (SSLSocketFactory.getDefault()
                .createSocket(host, port) as SSLSocket)
                .apply {
                    startHandshake()
                }
    } else {
        Socket(hostAddress, port)
    }
}

fun BufferedWriter.writeRequest(request: HttpRequest) = also { writer ->
    val resource = request.resourcePath
            .takeIf { resource -> resource.isNotBlank() }
            ?: "/"

    val queryParams = request.queryParams
            .toList()
            .joinToString("&") { (key, value) ->
                "$key=$value"
            }
            .takeIf { params -> params.isNotBlank() }
            ?.let { params -> "?$params" }
            ?: ""

    writer.write(
            """${request.rawMethod} $resource$queryParams HTTP/1.1
                        |${request.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }}
                        |
                        |${request.entity}
                        |
                        """.trimMargin().replace("\n", "\r\n")
    )
    writer.flush()
}