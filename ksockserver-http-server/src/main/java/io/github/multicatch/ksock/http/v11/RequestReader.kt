package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpMethod
import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.extractHeaders
import java.io.BufferedReader
import java.io.InputStream

private const val ENTITY_SIZE_HEADER = "content-length"

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

    HttpRequest(
            method = method,
            resourceUri = resourceWithoutParams,
            queryParams = queryParams,
            httpVersion = httpVersion,
            headers = headers,
            entity = extractEntity(contentLength),
            remoteAddress = remoteAddress
    )
}

fun BufferedReader.extractHeaders() = lineSequence().extractHeaders()

fun BufferedReader.extractEntity(contentLength: Long) = mutableListOf<Byte>()
        .also { byteList ->
            for (i in 1..contentLength) {
                byteList.add(read().toByte())
            }
        }.toByteArray()
        .let {
            String(it)
        }