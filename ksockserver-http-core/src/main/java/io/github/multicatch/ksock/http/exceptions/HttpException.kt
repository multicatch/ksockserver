package io.github.multicatch.ksock.http.exceptions

import io.github.multicatch.ksock.http.HttpStatus

abstract class HttpException(val httpStatus: HttpStatus, val response: String? = null) :
        Exception("Http Exception with status: $httpStatus and response $response")

class NotFoundException(response: String? = null )
    : HttpException(httpStatus = HttpStatus.NOT_FOUND, response = response)