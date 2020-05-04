package io.github.multicatch.handlers

import io.github.multicatch.http.HttpRequest
import io.github.multicatch.http.HttpResponse
import io.github.multicatch.http.HttpStatus

fun static(path: String): (HttpRequest) -> HttpResponse = {
    val responseEntity = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(path)
            ?.bufferedReader()
            ?.readText()
            ?: ""

    HttpResponse(
            status = HttpStatus.OK,
            headers = mapOf(
                    "Content-Type" to "text/html"
            ),
            entity = responseEntity
    )
}