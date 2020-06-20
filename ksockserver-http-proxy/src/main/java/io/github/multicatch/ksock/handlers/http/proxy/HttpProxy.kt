package io.github.multicatch.ksock.handlers.http.proxy

import io.github.multicatch.ksock.http.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory


fun HttpConfig.proxy(targetAddress: String) = apply {
    this.handler = { request ->
        val actualRequest = request.copy(
                headers = request.headers + ("Forwarded" to "for=${request.remoteAddress}")
        )

        val host = Regex("https?://([^/]*)").find(targetAddress)?.groupValues?.last()
                ?: targetAddress.substringBefore("/")

        socketOf(host).use {
            val reader = it.getInputStream().bufferedReader()
            val writer = it.getOutputStream().bufferedWriter()
            writer.writeRequest(actualRequest)

            val status = reader.readLine().extractHttpStatus()
            val headers = reader.lineSequence().extractHeaders()
            val entity = reader.readEntity(headers)

            ProxiedHttpResponse(
                    status = status,
                    originalHeaders = headers,
                    entity = entity
            )
        }
    }
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
            """${request.method.name} $resource$queryParams HTTP/1.1
                        |${request.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }}
                        |
                        |${request.entity}
                        |
                        """.trimMargin().replace("\n", "\r\n")
    )
    writer.flush()
}

fun BufferedReader.readEntity(headers: Map<String, String>) =
        mutableListOf<Byte>().also { byteList ->
            val contentLength = if (headers["content-encoding"]?.contains("gzip") == true) {
                Int.MAX_VALUE
            } else {
                headers["content-length"]?.toInt() ?: 0
            }

            val size = if (headers["content-type"]?.contains("utf-8") == true) {
                contentLength - 2
            } else {
                contentLength
            }

            while (byteList.size != size) {
                val byte = read()
                if (byte == -1) {
                    break
                }
                byteList += byte.toByte()
            }
        }.toByteArray()