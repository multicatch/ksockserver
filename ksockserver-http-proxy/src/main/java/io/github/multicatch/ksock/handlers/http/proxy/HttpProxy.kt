package io.github.multicatch.ksock.handlers.http.proxy

import io.github.multicatch.ksock.http.*
import java.net.Socket

fun HttpConfig.proxy(targetAddress: String) = apply {
    this.handler = { request ->
        val actualRequest = request.copy(
                headers = request.headers + ("Forwarded" to "for=${request.remoteAddress}")
        )

        val host = Regex("https?://([^/]*)").find(targetAddress)?.groupValues?.last()
                ?: targetAddress.substringBefore("/")

        val hostAddress = host.substringBefore(":")
        val port = host.substringAfter(":", "80").toInt()

        Socket(hostAddress, port).use {
            val reader = it.getInputStream().bufferedReader()
            val writer = it.getOutputStream().bufferedWriter()

            val resource = actualRequest.resourcePath
                    .takeIf { resource -> resource.isNotBlank() }
                    ?: "/"

            val queryParams = actualRequest.queryParams
                    .toList()
                    .joinToString("&") { (key, value) ->
                        "$key=$value"
                    }
                    .takeIf { params -> params.isNotBlank() }
                    ?.let { params -> "?$params" }
                    ?: ""

            writer.write(
                    """${actualRequest.method.name} $resource$queryParams HTTP/1.1
                        |${actualRequest.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" }}
                        |
                        |${actualRequest.entity}
                        |
                    """.trimMargin().replace("\n", "\r\n")
            )
            writer.flush()

            val status = reader.readLine().split(" ")[1].toInt().toHttpStatus()
            val headers = reader.lineSequence().extractHeaders()
            val contentLength = headers["content-length"]?.toInt() ?: 0
            val entity = mutableListOf<Byte>().also { byteList ->
                val size = if (headers["content-type"]?.contains("utf-8") == true) {
                    contentLength - 2
                } else {
                    contentLength
                }

                while (byteList.size != size) {
                    val byte = reader.read()
                    if (byte == -1) {
                        break
                    }
                    byteList += byte.toByte()
                }
            }.let { byteList ->
                String(byteList.toByteArray())
            }

            HttpResponse(
                    status = status,
                    headers = headers.filterNot { (key, _) ->
                        key.equals("server", ignoreCase = true) || key.equals("content-length", ignoreCase = true)
                    },
                    entity = entity.replace("\r\n", "\n")
            )
        }
    }
}
