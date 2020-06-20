package io.github.multicatch.ksock.handlers.http.proxy

import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.HttpStatus

data class ProxiedHttpResponse(
        override val status: HttpStatus,
        val originalHeaders: Map<String, String>,
        override val entity: ByteArray
) : HttpResponse {
    override val headers: Map<String, String>
        get() = originalHeaders.filterNot { (key, _) ->
            key.equals("server", ignoreCase = true)
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProxiedHttpResponse

        if (status != other.status) return false
        if (originalHeaders != other.originalHeaders) return false
        if (!entity.contentEquals(other.entity)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = status.hashCode()
        result = 31 * result + originalHeaders.hashCode()
        result = 31 * result + entity.contentHashCode()
        return result
    }
}