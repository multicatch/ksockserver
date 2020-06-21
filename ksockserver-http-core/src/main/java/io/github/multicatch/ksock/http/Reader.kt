package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.http.request.HttpInfo
import java.io.BufferedReader

const val ENTITY_SIZE_HEADER = "content-length"

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
                .joinToString()
                .toByteArray()
                .let { currentEntity + it }