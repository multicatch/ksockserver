package io.github.multicatch.ksock.example

import io.github.multicatch.ksock.handlers.http.proxy.proxy
import io.github.multicatch.ksock.handlers.staticIndex
import io.github.multicatch.ksock.handlers.staticPage
import io.github.multicatch.ksock.http.*
import io.github.multicatch.ksock.http.v1.gzip.GZipResponseWriter
import io.github.multicatch.ksock.http.v11.withHttp11
import io.github.multicatch.ksock.tcp.bindSecureTCP
import io.github.multicatch.ksock.tcp.bindTCP
import io.github.multicatch.ksock.tcp.selfSignedCertificate

fun main() {
    bindTCP(port = 8080, protocol = Http) {
        withHttp11()
        withResponseWriter(GZipResponseWriter())

        url(index("/")) {
            staticIndex("classpath:/")
        }

        url(exact("/example")) {
            staticPage("classpath:/index.html")
        }

        url(exact("/proxy")) {
            proxy("https://httpbin.org/")
        }

        alias(exact("/") to "/index.html")
    }.start()

    bindSecureTCP(
            port = 8443,
            protocol = Http,
            serverCertificate = selfSignedCertificate()
    ) {
        withHttp11()

        url(index("/")) {
            staticIndex("classpath:/")
        }

        url(exact("/proxy")) {
            proxy("https://httpbin.org/")
        }

        alias(exact("/") to "/index.html")
    }.start()
}