package io.github.multicatch

import io.github.multicatch.handlers.static

fun main() {
    bind(9000) {
        url("/") {
            static("index.html")
        }
    }
}