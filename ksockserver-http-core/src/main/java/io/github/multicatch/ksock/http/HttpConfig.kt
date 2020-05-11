package io.github.multicatch.ksock.http

class HttpConfig {
    lateinit var handler: (HttpRequest) -> HttpResponse
    var aliasRules: MutableList<Pair<String, String>> = mutableListOf()
}
