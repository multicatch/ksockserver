package io.github.multicatch.khttp.http

data class HttpRequest(
        val method: HttpMethod,
        val resourceUri: String,
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