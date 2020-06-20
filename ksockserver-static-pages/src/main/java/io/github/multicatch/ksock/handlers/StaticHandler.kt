package io.github.multicatch.ksock.handlers

import io.github.multicatch.ksock.http.HttpConfig
import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.PlaintextHttpResponse
import io.github.multicatch.ksock.http.StandardHttpStatus
import java.nio.charset.Charset

fun HttpConfig.staticPage(resourcePath: String) = apply {
    this.handler = {
        val responseEntity = resourcePath.load()
                ?.bufferedReader()
                ?.readText()
                ?: throw NotFoundException()

        PlaintextHttpResponse(
                status = StandardHttpStatus.OK,
                originalHeaders = mapOf(
                        "Content-Type" to "text/html; charset=${Charset.defaultCharset().name()}"
                ),
                stringEntity = responseEntity
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

        PlaintextHttpResponse(
                status = StandardHttpStatus.OK,
                originalHeaders = mapOf(
                        "Content-Type" to "text/html; charset=${Charset.defaultCharset().name()}"
                ),
                stringEntity = responseEntity
        )
    }
}