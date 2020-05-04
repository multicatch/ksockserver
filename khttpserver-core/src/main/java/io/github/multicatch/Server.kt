package io.github.multicatch

import io.github.multicatch.http.HttpRequest
import io.github.multicatch.http.HttpResponse
import java.net.ServerSocket
import java.util.concurrent.Executors

fun bind(port: Int, configure: ServerConfiguration.() -> Unit) {
    val configuration = ServerConfiguration(port = port).apply(configure)
    val dispatcher = RequestDispatcher(configuration, Executors.newFixedThreadPool(10))

    ServerSocket(configuration.port).use {
        while(true) {
            dispatcher.dispatch(it.accept())
        }
    }
}

fun ServerConfiguration.url(path: String, mapping: () -> (HttpRequest) -> HttpResponse) {
    urls[path] = mapping()
}

class ServerConfiguration(
        val port: Int = 0,
        val urls: MutableMap<String, (HttpRequest) -> HttpResponse> = mutableMapOf()
)