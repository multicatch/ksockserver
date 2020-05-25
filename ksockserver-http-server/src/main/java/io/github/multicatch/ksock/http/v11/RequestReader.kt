package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpMethod
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.extractHeaders
import java.io.BufferedReader
import java.io.InputStream

private const val ENTITY_SIZE_HEADER = "content-length"
private const val ENTITY_TYPE_HEADER = "content-type"

fun InputStream.readRequest(remoteAddress: String) = with(bufferedReader()) {
    val firstLine = readLine().split(" ")
    val method = HttpMethod.valueOf(firstLine.first())
    val resource = firstLine[1]
    val httpVersion = firstLine.getOrNull(2)

    val queryParams = resource.substringAfterLast("?")
            .split("&")
            .flatMap { it.split("=").zipWithNext() }
            .toMap()

    val resourceWithoutParams = resource.substringBeforeLast("?")

    val headers = extractHeaders()
    val contentLength = headers.getOrDefault(ENTITY_SIZE_HEADER, "0").toLong()
    val contentType = headers.getOrDefault(ENTITY_TYPE_HEADER, "text/plain")

    HttpRequest(
            method = method,
            resourceUri = resourceWithoutParams,
            queryParams = queryParams,
            httpVersion = httpVersion,
            headers = headers,
            entity = extractEntity(contentLength, contentType),
            remoteAddress = remoteAddress
    )
}

fun BufferedReader.extractHeaders() = lineSequence().extractHeaders()

fun BufferedReader.extractEntity(contentLength: Long, contentType: String) = mutableListOf<Byte>()
        .also { byteList ->
            val size = if (contentType == "utf-8") {
                contentLength - 2
            } else {
                contentLength
            }

            for (i in 1..size) {
                byteList.add(read().toByte())
            }
        }.toByteArray()
        .let {
            String(it)
        }