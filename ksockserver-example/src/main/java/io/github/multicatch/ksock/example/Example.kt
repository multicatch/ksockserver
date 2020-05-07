package io.github.multicatch.ksock.example

import io.github.multicatch.ksock.handlers.staticPage
import io.github.multicatch.ksock.http.url
import io.github.multicatch.ksock.http.v11.Http11
import io.github.multicatch.ksock.tcp.bindTcp

fun main() {
    bindTcp(port = 9000, protocol = Http11()) {
        url("/") {
            staticPage("index.html")
        }
    }.start()
}