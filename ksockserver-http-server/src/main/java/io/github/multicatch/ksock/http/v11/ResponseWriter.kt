package io.github.multicatch.ksock.http.v11

import io.github.multicatch.ksock.http.HttpResponse
import java.io.OutputStream

fun OutputStream.write(response: HttpResponse) {
    with(bufferedWriter()) {
        write("""HTTP/1.1 ${response.status.code} ${response.status.description}
Server: ksockserver
Content-Length: ${response.entityLength()}
${response.headers.toStringHeaders()}

${response.entity}
""".replace("\n", "\r\n"))
        flush()
    }
}

fun Map<String, String>.toStringHeaders() = map { (name, value) -> "$name: $value" }
        .joinToString("\n")