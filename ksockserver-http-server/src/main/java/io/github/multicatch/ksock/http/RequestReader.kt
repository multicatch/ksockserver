package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.EntityReader
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.request.HttpInfo
import java.io.BufferedReader

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

fun ByteArray.convertEntity(entityReaders: List<EntityReader>, headers: Map<String, String>) =
        entityReaders.fold(null as String?) { result, reader ->
            result ?: reader.read(headers, this)
        }
