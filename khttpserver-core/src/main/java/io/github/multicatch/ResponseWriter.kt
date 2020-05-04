package io.github.multicatch

import io.github.multicatch.http.HttpResponse
import java.io.OutputStream

fun OutputStream.write(response: HttpResponse) {
    with(bufferedWriter()) {
        write("""HTTP/1.1 ${response.status.code} ${response.status.description}
Server: khttpserver
Content-Length: ${response.entityLength()}
${response.headers.toStringHeaders()}

${response.entity}
""".replace("\n", "\r\n"))
        flush()
    }
}

fun Map<String, String>.toStringHeaders() = map { (name, value) -> "$name: $value" }
        .joinToString("\n")