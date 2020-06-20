package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpResponse
import java.io.OutputStream

fun OutputStream.write(response: HttpResponse) {
    with(bufferedWriter()) {
        write("""HTTP/1.1 ${response.status.code} ${response.status.description}${'\r'}
Server: ksockserver${'\r'}
${response.headers.toStringHeaders()}${'\r'}
${'\r'}
${String(response.entity)}${'\r'}
""")
        flush()
    }
}

fun Map<String, String>.toStringHeaders() = map { (name, value) -> "$name: $value" }
        .joinToString("\r\n")