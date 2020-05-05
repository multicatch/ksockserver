package io.github.multicatch.khttp.http

import io.github.multicatch.khttp.ServerConfiguration
import io.github.multicatch.khttp.ServerProtocolProcessor

interface HttpProtocol : ServerProtocolProcessor {
    val urls: MutableMap<String, (HttpRequest) -> HttpResponse>
}

fun ServerConfiguration<out HttpProtocol>.url(path: String, mapping: () -> (HttpRequest) -> HttpResponse) {
    protocol.urls[path] = mapping()
}
