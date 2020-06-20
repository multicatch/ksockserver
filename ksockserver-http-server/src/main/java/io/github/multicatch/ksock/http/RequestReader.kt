package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.request.HttpInfo
import jdk.internal.util.xml.impl.Input
import java.io.BufferedReader
import java.io.InputStream
import java.nio.charset.Charset

const val ENTITY_SIZE_HEADER = "content-length"
const val ENTITY_TYPE_HEADER = "content-type"

fun InputStream.readRequest(remoteAddress: String, headerReaders: List<HeaderReader>, entityReaders: List<EntityReader>): HttpRequest = with(bufferedReader()) {
    val httpInfo = httpInfo(remoteAddress)

    val resourceWithoutParams = httpInfo.resource.substringBeforeLast("?")

    val headers = headerReaders.fold(null as Map<String, String>?) { result, reader ->
        result ?: reader.read(httpInfo, this)
    } ?: error("Unable to read request headers")

    val contentLength = headers.getOrDefault(ENTITY_SIZE_HEADER, "0").toLong()
    val contentType = headers.getOrDefault(ENTITY_TYPE_HEADER, "text/plain")

    val rawEntity = extractEntity(contentLength, contentType)
    val entity = entityReaders.fold(null as String?) { result, reader ->
        result ?: reader.read(headers, rawEntity)
    }

    HttpRequest(
            rawMethod = httpInfo.method,
            resourceUri = resourceWithoutParams,
            queryParams = httpInfo.queryParams(),
            httpVersion = httpInfo.version,
            headers = headers,
            rawEntity = rawEntity,
            entity = entity,
            remoteAddress = remoteAddress
    )
}

fun BufferedReader.httpInfo(remoteAddress: String): HttpInfo {
    val firstLine = readLine()?.split(" ") ?: error("Cannot parse HTTP request")
    val method = firstLine.first()
    val resource = firstLine[1]
    val httpVersion = firstLine.getOrNull(2)

    return HttpInfo(
            method,
            resource,
            httpVersion,
            remoteAddress
    )
}

fun HttpInfo.queryParams() = resource
        .substringAfterLast("?")
        .split("&")
        .flatMap { it.split("=").zipWithNext() }
        .toMap()

fun InputStream.extractEntity(contentLength: Long, contentType: String) = mutableListOf<Byte>()
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