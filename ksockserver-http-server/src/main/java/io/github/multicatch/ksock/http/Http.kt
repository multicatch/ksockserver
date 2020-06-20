package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.exception.exceptionMapperOf
import io.github.multicatch.ksock.http.exception.mapOrReturnDefault
import io.github.multicatch.ksock.http.exceptions.ExceptionMapper
import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.response.ResponseWriter
import java.net.Socket
import kotlin.reflect.KClass

object Http : HttpProtocol {
    override val urls: MutableList<Pair<UrlPattern, (HttpRequest) -> HttpResponse>> = mutableListOf()
    override val entityReaders: MutableList<EntityReader> = mutableListOf()
    override val headerReaders: MutableList<HeaderReader> = mutableListOf()
    override val responseWriters: MutableList<ResponseWriter> = mutableListOf()
    override val exceptionMappers: MutableMap<KClass<out Throwable>, ExceptionMapper<out Throwable>> = mutableMapOf()

    override fun process(connection: Socket) {
        val request = connection.getInputStream()
                .readRequest(connection.inetAddress.hostAddress, headerReaders.toList(), entityReaders.toList())
        println(request)

        val response = try {
            val (urlPattern, handler) = request.resourceUri.handler()
                    ?: throw NotFoundException()
            val requestWithContext = request.copy(
                    contextPath = urlPattern.basePath,
                    resourcePath = urlPattern.trimBasePath(request.resourceUri)
            )

            handler(requestWithContext)
        } catch (throwable: Throwable) {
            @Suppress("UNCHECKED_CAST")
            (exceptionMapperOf(throwable::class, exceptionMappers) as ExceptionMapper<Throwable>?)
                    .mapOrReturnDefault(request, throwable)
        }

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
            .find { (url, _) ->
                url.matches(this)
            }
}
