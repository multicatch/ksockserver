package io.github.multicatch.ksock.http.request

import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse

interface RequestHandler {
    fun handle(request: HttpRequest): HttpResponse
    fun interrupt()
    fun resume(): HttpResponse
}

interface RequestHandlerFactory {
    fun create(): RequestHandler
}