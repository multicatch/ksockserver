package io.github.multicatch.khttp.example

import io.github.multicatch.khttp.handlers.static
import io.github.multicatch.khttp.bind
import io.github.multicatch.khttp.url

fun main() {
    bind(9000) {
        url("/") {
            static("index.html")
        }
    }
}