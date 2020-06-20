package io.github.multicatch.ksock.http.v1.gzip

import io.github.multicatch.ksock.http.HttpRequest
import io.github.multicatch.ksock.http.HttpResponse
import io.github.multicatch.ksock.http.response.ResponseWriter
import io.github.multicatch.ksock.http.response.toStringHeaders
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class GZipResponseWriter : ResponseWriter {
    override fun write(request: HttpRequest, response: HttpResponse): ByteArray? {
        if (response.headers.any { (key, _) -> key.equals("content-encoding", true) }) {
            return null
        }
        if (request.headers["accept-encoding"]?.contains("gzip") != true) {
            return null
        }

        val headersWithoutLength = response.headers
                .filterNot { (key, _ ) -> key.equals("content-length", true) }

        val compressedResponse = response.compress()

        return """HTTP/1.1 ${response.status.code} ${response.status.description}${'\r'}
Server: ksockserver${'\r'}
Content-Encoding: gzip${'\r'}
Content-Length: ${compressedResponse.size}${'\r'}
${headersWithoutLength.toStringHeaders()}${'\r'}
${'\r'}
""".toByteArray() + compressedResponse
    }

    private fun HttpResponse.compress(): ByteArray = ByteArrayOutputStream(this.entity.size)
            .use {
                GZIPOutputStream(it).use { gzip ->
                    gzip.write(this.entity)
                }
                it.toByteArray()
            }
}