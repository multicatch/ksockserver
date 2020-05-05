package io.github.multicatch.khttp.example

import io.github.multicatch.khttp.handlers.static
import io.github.multicatch.khttp.bind
import io.github.multicatch.khttp.http.url
import io.github.multicatch.khttp.http.v11.Http11

fun main() {
    bind(9000, Http11()) {
        url("/") {
            static("index.html")
        }
    }
}