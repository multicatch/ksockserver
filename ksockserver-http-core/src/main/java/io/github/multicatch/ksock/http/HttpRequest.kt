package io.github.multicatch.ksock.http

data class HttpRequest(
        val rawMethod: String,
        val contextPath: String = "/",
        val resourceUri: String,
        val resourcePath: String = resourceUri,
        val queryParams: Map<String, String>,
        val httpVersion: String?,
        val headers: Map<String, String>,
        val rawEntity: ByteArray,
        val entity: String?,
        val remoteAddress: String
) {
    val method: HttpMethod
        get() = HttpMethod.valueOf(rawMethod)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HttpRequest

        if (rawMethod != other.rawMethod) return false
        if (contextPath != other.contextPath) return false
        if (resourceUri != other.resourceUri) return false
        if (resourcePath != other.resourcePath) return false
        if (queryParams != other.queryParams) return false
        if (httpVersion != other.httpVersion) return false
        if (headers != other.headers) return false
        if (!rawEntity.contentEquals(other.rawEntity)) return false
        if (entity != other.entity) return false
        if (remoteAddress != other.remoteAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawMethod.hashCode()
        result = 31 * result + contextPath.hashCode()
        result = 31 * result + resourceUri.hashCode()
        result = 31 * result + resourcePath.hashCode()
        result = 31 * result + queryParams.hashCode()
        result = 31 * result + (httpVersion?.hashCode() ?: 0)
        result = 31 * result + headers.hashCode()
        result = 31 * result + rawEntity.contentHashCode()
        result = 31 * result + (entity?.hashCode() ?: 0)
        result = 31 * result + remoteAddress.hashCode()
        return result
    }
}

enum class HttpMethod {
    GET,
    POST,
    PUT,
    DELETE
}

fun Sequence<String>.extractHeaders() = takeWhile { line ->
            line.isNotEmpty()
        }
        .map { line ->
            line.asHeader()
        }
        .toMap()

fun String.asHeader() = let { header ->
    with(header.indexOf(":")) {
        header.substring(0, this).toLowerCase() to header.substring(this + 1).trim()
    }
}