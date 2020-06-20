package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.exception.exceptionMapperOf
import io.github.multicatch.ksock.http.exception.mapOrReturnDefault
import io.github.multicatch.ksock.http.exceptions.ExceptionMapper
import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.request.HttpInfo
import io.github.multicatch.ksock.http.response.ResponseWriter
import io.github.multicatch.ksock.tcp.MessageProcessor
import io.github.multicatch.ksock.tcp.MessageReader
import io.github.multicatch.ksock.tcp.MessageWriter
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.OutputStream
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore
import kotlin.reflect.KClass

object Http : HttpProtocol {
    override val urls: MutableList<Pair<UrlPattern, (HttpRequest) -> HttpResponse>> = mutableListOf()
    override val entityReaders: MutableList<EntityReader> = mutableListOf()
    override val headerReaders: MutableList<HeaderReader> = mutableListOf()
    override val responseWriters: MutableList<ResponseWriter> = mutableListOf()
    override val exceptionMappers: MutableMap<KClass<out Throwable>, ExceptionMapper<out Throwable>> = mutableMapOf()

    override fun reader(connection: Socket): MessageReader<HttpRequest> {
        val bufferedReader = connection.getInputStream().bufferedReader()
        return HttpMessageReader(connection.inetAddress.hostAddress, bufferedReader, headerReaders.toList(), entityReaders.toList())
    }

    override fun writer(connection: Socket): MessageWriter<ByteArray> {
        return HttpMessageWriter(BufferedOutputStream(connection.getOutputStream(), 1024))
    }

    override fun processor(outgoing: LinkedBlockingDeque<ByteArray>, socket: Socket): MessageProcessor<HttpRequest, ByteArray> {
        return HttpMessageProcessor(outgoing, responseWriters, urls.toList(), exceptionMappers.toMap())
    }
}

class HttpMessageReader(
        private val remoteAddress: String,
        private val bufferedReader: BufferedReader,
        private val headerReaders: List<HeaderReader>,
        private val entityReaders: List<EntityReader>
) : MessageReader<HttpRequest> {
    private var httpInfo: HttpInfo? = null
    private var headers: MutableMap<String, String> = mutableMapOf()
    private var headersRead = false
    private var rawEntity: ByteArray = byteArrayOf()

    override fun read(): HttpRequest {
        val info = httpInfo ?: bufferedReader.httpInfo(remoteAddress)

        httpInfo = info

        while (!headersRead) {
            val header = bufferedReader.readSingleHeader(info, headerReaders)
            if (header != null) {
                headers[header.first] = header.second
            } else {
                headersRead = true
            }
        }
        val contentLength = headers.getOrDefault(ENTITY_SIZE_HEADER, "0").toInt()
        while (rawEntity.size < contentLength) {
            rawEntity = bufferedReader.readNextEntityByte(rawEntity, contentLength)
        }

        val entity = rawEntity.convertEntity(entityReaders, headers)
        val request = requestOf(info, headers, rawEntity, entity)

        httpInfo = null
        headers = mutableMapOf()
        headersRead = false
        rawEntity = byteArrayOf()

        return request
    }
}

class HttpMessageWriter(
        private val outputStream: BufferedOutputStream
) : MessageWriter<ByteArray> {
    private var bytesToWrite: LinkedBlockingDeque<Byte> = LinkedBlockingDeque()

    override fun write(message: ByteArray) {
        bytesToWrite.addAll(message.toList())
        resume()
    }

    override fun resume() {
        with(outputStream) {
            while (bytesToWrite.isNotEmpty()) {
                write(bytesToWrite.takeFirst().toInt())
            }
            flush()
        }
    }
}

class HttpMessageProcessor(
        private val outgoingQueue: LinkedBlockingDeque<ByteArray>,
        private val responseWriters: List<ResponseWriter>,
        private val urls: List<Pair<UrlPattern, (HttpRequest) -> HttpResponse>>,
        private val exceptionMappers: Map<KClass<out Throwable>, ExceptionMapper<out Throwable>>
) : MessageProcessor<HttpRequest, ByteArray> {

    private var currentMessage: HttpRequest? = null
    private var currentResponse: HttpResponse? = null
    private var currentRawResponse: ByteArray? = null

    override fun process(message: HttpRequest) {
        println(message)
        currentMessage = message
        resume()
    }

    override fun resume() {
        val message = currentMessage ?: return

        val response = currentResponse ?: try {
            message.toResponse()
        } catch (throwable: Throwable) {
            @Suppress("UNCHECKED_CAST")
            (exceptionMapperOf(throwable::class, exceptionMappers) as ExceptionMapper<Throwable>?)
                    .mapOrReturnDefault(message, throwable)
        }

        currentResponse = response

        val rawResponse = currentRawResponse ?: responseWriters
                .toList()
                .fold(null as ByteArray?) { result, writer ->
                    result ?: writer.write(message, response)
                } ?: error("Cannot write response")

        currentRawResponse = rawResponse

        outgoingQueue.offer(rawResponse)

        currentMessage = null
        currentResponse = null
        currentRawResponse = null
    }

    override fun interrupt() {
        if (currentMessage == null && currentResponse != null) {
            currentResponse = null
        }
        if (currentResponse == null && currentRawResponse != null) {
            currentRawResponse = null
        }
    }

    private fun HttpRequest.toResponse(): HttpResponse {
        val (urlPattern, handler) = resourceUri.handler()
                ?: throw NotFoundException()
        val requestWithContext = this.copy(
                contextPath = urlPattern.basePath,
                resourcePath = urlPattern.trimBasePath(resourceUri)
        )

        return handler(requestWithContext)
    }

    private fun String.handler() = urls.toList()
            .find { (url, _) ->
                url.matches(this)
            }
}