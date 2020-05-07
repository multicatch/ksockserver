package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpMethod
import io.github.multicatch.ksock.http.HttpRequest
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

    val headers = extractHeaders()
    val contentLength = headers.getOrDefault(ENTITY_SIZE_HEADER, "0").toLong()

    HttpRequest(
            method = method,
            resourceUri = resource,
            queryParams = queryParams,
            httpVersion = httpVersion,
            headers = headers,
            entity = extractEntity(contentLength),
            remoteAddress = remoteAddress
    )
}

fun BufferedReader.extractHeaders() = lineSequence()
        .takeWhile { line ->
            line.isNotBlank()
        }
        .map { header ->
            with(header.indexOf(":")) {
                header.substring(0, this).toLowerCase() to header.substring(this + 1).trim()
            }
        }
        .toMap()

fun BufferedReader.extractEntity(contentLength: Long) = mutableListOf<Byte>()
        .also { byteList ->
            for (i in 1..contentLength) {
                byteList.add(read().toByte())
            }
        }.toByteArray()
        .let {
            String(it)
        }