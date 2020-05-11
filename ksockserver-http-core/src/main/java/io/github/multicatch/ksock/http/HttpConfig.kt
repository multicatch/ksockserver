package io.github.multicatch.ksock.http

class HttpConfig {
    lateinit var handler: (HttpRequest) -> HttpResponse
}
