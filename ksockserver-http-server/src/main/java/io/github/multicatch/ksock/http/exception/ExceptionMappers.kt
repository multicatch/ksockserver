package io.github.multicatch.ksock.http.exception

import io.github.multicatch.ksock.http.*
import io.github.multicatch.ksock.http.exceptions.ExceptionMapper
import io.github.multicatch.ksock.tcp.TcpServerConfiguration
import org.apache.logging.log4j.LogManager
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses

inline fun <reified T: Throwable> TcpServerConfiguration<HttpRequest, ByteArray, out HttpProtocol>.withExceptionMapper(exceptionMapper: ExceptionMapper<T>) {
    protocol.exceptionMappers[T::class] = exceptionMapper
}

fun <T: Throwable> exceptionMapperOf(
        type: KClass<T>,
        exceptionMappers: Map<KClass<out Throwable>, ExceptionMapper<out Throwable>>
): ExceptionMapper<out Throwable>? {
    if (type == Throwable::class) {
        return null
    }

    val mapper = exceptionMappers[type]
    if (mapper == null) {
        val supertypes = type.allSuperclasses
                .filterIsInstance<KClass<out Throwable>>()

        for (supertype in supertypes) {
            val superMapper = exceptionMapperOf(supertype, exceptionMappers)
            if (superMapper != null) {
                return superMapper
            }
        }
        return null
    } else {
        return mapper
    }
}

fun <T: Throwable> ExceptionMapper<T>?.mapOrReturnDefault(
        request: HttpRequest,
        throwable: T,
        defaultResponse: HttpResponse = DEFAULT_RESPONSE
) =
        this?.let {
            try {
                it.map(request, throwable)
            } catch (throwable: Throwable) {
                logger.error("Got an error while looking for an exception mapper", throwable)
                defaultResponse
            }
        } ?: defaultResponse.also {
            logger.error("Got an error while processing a request from ${request.remoteAddress}", throwable)
        }

val DEFAULT_RESPONSE = PlaintextHttpResponse(
        status = StandardHttpStatus.INTERNAL_SERVER_ERROR,
        originalHeaders = mapOf(
                "Content-Type" to "text/plain"
        ),
        textEntity = "Internal server error"
)

private val logger = LogManager.getLogger(ExceptionMapper::class.java)