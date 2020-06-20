package io.github.multicatch.ksock.http

data class HttpRequest(
        val method: HttpMethod,
        val contextPath: String = "/",
        val resourceUri: String,
        val resourcePath: String = resourceUri,
        val queryParams: Map<String, String>,
        val httpVersion: String?,
        val headers: Map<String, String>,
        val entity: String,
        val remoteAddress: String
)

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE
}

fun Sequence<String>.extractHeaders() = takeWhile { line ->
            line.isNotEmpty()
        }
        .map { header ->
            with(header.indexOf(":")) {
                header.substring(0, this).toLowerCase() to header.substring(this + 1).trim()
            }
        }
        .toMap()