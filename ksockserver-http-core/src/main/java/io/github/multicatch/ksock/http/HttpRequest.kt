package io.github.multicatch.ksock.http

data class HttpRequest(
        val method: HttpMethod,
        val resourceUri: String,
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