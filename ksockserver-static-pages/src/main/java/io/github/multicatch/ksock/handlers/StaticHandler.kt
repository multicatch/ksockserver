package io.github.multicatch.ksock.handlers

import io.github.multicatch.ksock.http.HttpConfig
import io.github.multicatch.ksock.http.exceptions.NotFoundException
import io.github.multicatch.ksock.http.PlaintextHttpResponse
import io.github.multicatch.ksock.http.StandardHttpStatus
import org.apache.logging.log4j.LogManager
import java.nio.charset.Charset

fun HttpConfig.staticPage(resourcePath: String) = apply {
    this.handler = {
        logger.debug("Looking for static ${resourcePath}")
        val responseEntity = resourcePath.load()
                ?.bufferedReader()
                ?.readText()
                ?: throw NotFoundException()

        PlaintextHttpResponse(
                status = StandardHttpStatus.OK,
                originalHeaders = mapOf(
                        "Content-Type" to "text/html; charset=${Charset.defaultCharset().name()}"
                ),
                textEntity = responseEntity
        )
    }
}

fun HttpConfig.staticIndex(path: String) = apply {
    this.handler = {
        val localResource = "${path.trimEnd('/')}/${it.resourcePath}"

        logger.debug("Looking for static ${localResource}")
        val responseEntity = localResource.load()
                ?.bufferedReader()
                ?.readText()
                ?: throw NotFoundException()

        PlaintextHttpResponse(
                status = StandardHttpStatus.OK,
                originalHeaders = mapOf(
                        "Content-Type" to "text/html; charset=${Charset.defaultCharset().name()}"
                ),
                textEntity = responseEntity
        )
    }
}

private val logger = LogManager.getLogger(HttpConfig::class.java)