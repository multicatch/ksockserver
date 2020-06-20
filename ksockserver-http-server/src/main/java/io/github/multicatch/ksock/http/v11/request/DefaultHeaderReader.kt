package io.github.multicatch.ksock.http.v11.request

import io.github.multicatch.ksock.http.asHeader
import io.github.multicatch.ksock.http.extractHeaders
import io.github.multicatch.ksock.http.request.HeaderReader
import io.github.multicatch.ksock.http.request.HttpInfo
import java.io.BufferedReader
import java.io.InputStream

class DefaultHeaderReader : HeaderReader {
    override fun readSingle(httpInfo: HttpInfo, line: String): Pair<String, String>? =
            line.takeIf { it.isNotBlank() }
                    ?.asHeader()
}