package io.github.multicatch.ksock.http.exceptions

import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.PlaintextHttpResponse

class DefaultHttpExceptionMapper : ExceptionMapper<HttpException> {
    override fun map(request: HttpRequest, throwable: HttpException): HttpResponse =
            PlaintextHttpResponse(
                    status = throwable.httpStatus,
                    originalHeaders = mapOf(
                            "Content-Type" to "text/plain"
                    ),
                    textEntity = throwable.response ?: "OOPSIE WOOPSIE!! Uwu We made a fucky wucky!! A wittle fucko boingo!"
            )
}