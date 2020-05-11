package io.github.multicatch.ksock.handlers

import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.HttpStatus

fun staticPage(resourcePath: String): (HttpRequest) -> HttpResponse = {
    val responseEntity = resourcePath.load()
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

fun staticIndex(path: String): (HttpRequest) -> HttpResponse = {
    val localResource = "${path.trimEnd('/')}/${it.resourcePath}"

    val responseEntity = localResource.load()
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