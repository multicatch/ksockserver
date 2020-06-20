package io.github.multicatch.ksock.http.exceptions

import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.HttpStatus
import io.github.multicatch.ksock.http.StandardHttpStatus

interface ExceptionMapper<T : Throwable> {
    fun map(request: HttpRequest, throwable: T): HttpResponse
}

abstract class HttpException(val httpStatus: HttpStatus, val response: String? = null) :
        Exception("Http Exception with status: $httpStatus and response $response")

class NotFoundException(response: String? = null )
    : HttpException(httpStatus = StandardHttpStatus.NOT_FOUND, response = response)