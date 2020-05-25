package io.github.multicatch.ksock.example

import io.github.multicatch.ksock.handlers.http.proxy.proxy
import io.github.multicatch.ksock.handlers.php.php
import io.github.multicatch.ksock.handlers.staticIndex
import io.github.multicatch.ksock.handlers.staticPage
import io.github.multicatch.ksock.http.alias
import io.github.multicatch.ksock.http.url
import io.github.multicatch.ksock.http.v11.Http11
import io.github.multicatch.ksock.tcp.bindTcp

fun main() {
    bindTcp(port = 8080, protocol = Http11()) {
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
            proxy("http://httpbin.org/")
        }

        alias("/", "/index.html")
    }.start()
}