package io.github.multicatch.ksock.handlers

import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.HttpStatus

fun staticPage(path: String): (HttpRequest) -> HttpResponse = {
    val responseEntity = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(path)
            ?.bufferedReader()
            ?.readText()
            ?: throw NotFoundException()

    HttpResponse(
            status = HttpStatus.OK,
            headers = mapOf(
                    "Content-Type" to "text/html"
            ),
            entity = responseEntity
    )
}