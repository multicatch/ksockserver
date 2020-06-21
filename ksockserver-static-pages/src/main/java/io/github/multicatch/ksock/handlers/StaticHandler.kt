package io.github.multicatch.ksock.handlers

import io.github.multicatch.ksock.http.*
import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.request.RequestHandler
import io.github.multicatch.ksock.http.request.RequestHandlerFactory
import org.apache.logging.log4j.LogManager
import java.io.InputStream
import java.nio.charset.Charset

fun HttpConfig.staticPage(resourcePath: String) = apply {
    this.handler = StaticHandlerFactory(resourcePath, false)
}

fun HttpConfig.staticIndex(path: String) = apply {
    this.handler = StaticHandlerFactory(path, true)
}

class StaticHandlerFactory(
        private val path: String,
        private val index: Boolean
) : RequestHandlerFactory {
    override fun create(): RequestHandler {
        return StaticHandler(path, index)
    }
}

class StaticHandler(
        private val path: String,
        private val index: Boolean
) : RequestHandler {
    private lateinit var localResource: String
    private lateinit var inputStream: InputStream
    private lateinit var contentType: String
    private var content: ByteArray = byteArrayOf()

    override fun handle(request: HttpRequest): HttpResponse {
        localResource = buildString {
            append(path.trimEnd('/'))
            if (index) {
                append("/${request.resourcePath}")
            }
        }

        return resume()
    }

    override fun interrupt() {
        // do nothing
    }

    override fun resume(): HttpResponse {
        if (!this::inputStream.isInitialized) {
            logger.debug("Looking for static $localResource")
            inputStream = localResource.load() ?: throw NotFoundException()
        }

        if (!this::contentType.isInitialized) {
            contentType = localResource.substringAfterLast('.')
            if (contentType.length > 4) {
                contentType = "plain"
            }
            contentType = "text/$contentType; charset=${Charset.defaultCharset().name()}"
            logger.debug("Content type of $localResource resolved to $contentType")
        }

        var byte = inputStream.read()
        while (byte >= 0) {
            content += byte.toByte()
            byte = inputStream.read()
        }

        return PlaintextHttpResponse(
                status = StandardHttpStatus.OK,
                originalHeaders = mapOf(
                        "Content-Type" to contentType
                ),
                textEntity = String(content)
        )
    }

}

private val logger = LogManager.getLogger(StaticHandler::class.java)