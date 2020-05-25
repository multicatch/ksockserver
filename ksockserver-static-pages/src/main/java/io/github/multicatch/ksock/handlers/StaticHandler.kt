package io.github.multicatch.ksock.handlers

import io.github.multicatch.ksock.http.HttpConfig
import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.StandardHttpStatus

fun HttpConfig.staticPage(resourcePath: String) = apply {
    this.handler = {
        val responseEntity = resourcePath.load()
                ?.bufferedReader()
                ?.readText()
                ?: throw NotFoundException()

        HttpResponse(
                status = StandardHttpStatus.OK,
                headers = mapOf(
                        "Content-Type" to "text/html"
                ),
                entity = responseEntity
        )
    }
}

fun HttpConfig.staticIndex(path: String) = apply {
    this.handler = {
        val localResource = "${path.trimEnd('/')}/${it.resourcePath}"

        val responseEntity = localResource.load()
                ?.bufferedReader()
                ?.readText()
                ?: throw NotFoundException()

        HttpResponse(
                status = StandardHttpStatus.OK,
                headers = mapOf(
                        "Content-Type" to "text/html"
                ),
                entity = responseEntity
        )
    }
}