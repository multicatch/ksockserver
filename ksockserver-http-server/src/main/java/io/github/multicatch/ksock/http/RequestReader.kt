package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.request.HttpInfo
import java.io.BufferedReader

const val ENTITY_SIZE_HEADER = "content-length"
const val ENTITY_TYPE_HEADER = "content-type"

fun requestOf(httpInfo: HttpInfo, headers: Map<String, String>, rawEntity: ByteArray, entity: String?): HttpRequest {
    val resourceWithoutParams = httpInfo.resource.substringBeforeLast("?")
    return HttpRequest(
            rawMethod = httpInfo.method,
            resourceUri = resourceWithoutParams,
            queryParams = httpInfo.queryParams(),
            httpVersion = httpInfo.version,
            headers = headers,
            rawEntity = rawEntity,
            entity = entity,
            remoteAddress = httpInfo.remoteAddress
    )
}

fun BufferedReader.readSingleHeader(httpInfo: HttpInfo, headerReaders: List<HeaderReader>): Pair<String, String>? {
    if (!this.ready()) return null
    val line = readLine()

    return headerReaders.fold(null as Pair<String, String>?) { result, reader ->
        result ?: reader.readSingle(httpInfo, line)
    }
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

fun BufferedReader.readNextEntityByte(currentEntity: ByteArray, contentLength: Int) =
        mutableListOf<Char>()
                .also { charList ->
                    if (currentEntity.size < contentLength) {
                        charList.add(read().toChar())
                    }
                }
                .toString()
                .toByteArray()
                .let { currentEntity + it }

fun ByteArray.convertEntity(entityReaders: List<EntityReader>, headers: Map<String, String>) =
        entityReaders.fold(null as String?) { result, reader ->
            result ?: reader.read(headers, this)
        }
