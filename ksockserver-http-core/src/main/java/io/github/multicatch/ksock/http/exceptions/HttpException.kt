package io.github.multicatch.ksock.http.exceptions

import io.github.multicatch.ksock.http.HttpStatus
import io.github.multicatch.ksock.http.StandardHttpStatus

abstract class HttpException(val httpStatus: HttpStatus, val response: String? = null) :
        Exception("Http Exception with status: $httpStatus and response $response")

class NotFoundException(response: String? = null )
    : HttpException(httpStatus = StandardHttpStatus.NOT_FOUND, response = response)