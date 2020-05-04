package io.github.multicatch.khttp.handlers

import io.github.multicatch.khttp.http.exceptions.NotFoundException
import io.github.multicatch.khttp.http.HttpRequest
import io.github.multicatch.khttp.http.HttpResponse
import io.github.multicatch.khttp.http.HttpStatus

fun static(path: String): (HttpRequest) -> HttpResponse = {
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