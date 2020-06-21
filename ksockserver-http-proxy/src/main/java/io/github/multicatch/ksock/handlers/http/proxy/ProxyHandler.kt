package io.github.multicatch.ksock.handlers.http.proxy

import io.github.multicatch.ksock.http.*
import io.github.multicatch.ksock.http.request.RequestHandler
import io.github.multicatch.ksock.http.request.RequestHandlerFactory
import org.apache.logging.log4j.LogManager
import java.io.BufferedReader
import java.net.Socket

class ProxyHandlerFactory(
        private val targetAddress: String
) : RequestHandlerFactory {
    override fun create(): RequestHandler {
        return ProxyHandler(targetAddress)
    }
}

class ProxyHandler(
        private val targetAddress: String
) : RequestHandler {
    private lateinit var actualRequest: HttpRequest
    private lateinit var host: String
    private lateinit var socket: Socket
    private var requestSent: Boolean = false
    private lateinit var bufferedReader: BufferedReader
    private lateinit var status: HttpStatus
    private val headers: MutableMap<String, String> = mutableMapOf()
    private var headersRead: Boolean = false
    private var rawEntity: ByteArray = byteArrayOf()
    private lateinit var result: HttpResponse

    override fun handle(request: HttpRequest): HttpResponse {
        actualRequest = request.copy(
                headers = request.headers + ("Forwarded" to "for=${request.remoteAddress}")
        )

        return resume()
    }

    override fun interrupt() {
        // do nothing
    }

    override fun resume(): HttpResponse {
        if (!this::host.isInitialized) {
            host = Regex("https?://([^/]*)").find(targetAddress)?.groupValues?.last()
                    ?: targetAddress.substringBefore("/")
        }

        logger.debug("Starting a proxy request to ${targetAddress}")
        logger.trace("Connecting to ${host}")
        if (!this::socket.isInitialized) {
            socket = socketOf(host)
        }

        if (!this::bufferedReader.isInitialized) {
            bufferedReader = socket.getInputStream().bufferedReader()
        }

        if (!requestSent) {
            socket.getOutputStream().bufferedWriter().writeRequest(actualRequest)
            requestSent = true
        }
        logger.trace("Proxy request to ${targetAddress} sent.")

        if (!this::status.isInitialized) {
            status = bufferedReader.readLine().extractHttpStatus()
        }

        logger.trace("Reading headers from ${targetAddress}.")
        while (!headersRead) {
            val header = bufferedReader.readLine()
                    .takeIf { it.isNotBlank() }
                    ?.asHeader()
            if (header != null) {
                headers[header.first] = header.second
            } else {
                headersRead = true
            }
        }

        logger.trace("Reading proxied entity of message from ${host}")
        val contentLength = headers.getOrDefault(ENTITY_SIZE_HEADER, "0").toInt()
        while (rawEntity.size < contentLength) {
            rawEntity = bufferedReader.readNextEntityByte(rawEntity, contentLength)
        }

        if (!this::result.isInitialized) {
            result = ProxiedHttpResponse(
                    status = status,
                    originalHeaders = headers,
                    entity = rawEntity
            )
        }

        if (!socket.isClosed) {
            socket.close()
        }

        return result
    }
}

private val logger = LogManager.getLogger(ProxyHandler::class.java)