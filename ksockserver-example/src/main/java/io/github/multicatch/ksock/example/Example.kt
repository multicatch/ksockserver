package io.github.multicatch.ksock.example

import io.github.multicatch.ksock.handlers.http.proxy.proxy
import io.github.multicatch.ksock.handlers.php.php
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

        url("/") {
            staticIndex("classpath:/")
        }
        url("/example") {
            staticPage("classpath:/index.html")
        }
        url("/php") {
            php("./ksockserver-example/src/main/resources")
            alias("/", "/index.php")
        }
        url("/proxy") {
            proxy("https://httpbin.org/")
        }

        alias("/", "/index.html")
    }.start()

    bindSecureTCP(
            port = 8443,
            protocol = Http,
            serverCertificate = selfSignedCertificate()
    ) {
        withHttp11()

        url("/") {
            staticIndex("classpath:/")
        }

        url("/proxy") {
            proxy("https://httpbin.org/")
        }

        alias("/", "/index.html")
    }.start()
}