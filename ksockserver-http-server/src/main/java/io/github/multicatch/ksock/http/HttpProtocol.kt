package io.github.multicatch.ksock.http

import io.github.multicatch.ksock.tcp.TcpServerConfiguration
import io.github.multicatch.ksock.tcp.TcpProtocolProcessor

interface HttpProtocol : TcpProtocolProcessor {
    val urls: MutableList<Pair<String, (HttpRequest) -> HttpResponse>>
}

fun TcpServerConfiguration<out HttpProtocol>.url(baseUrl: String, configuration: HttpConfig.() -> Unit) {
    val config = HttpConfig().apply(configuration)
    protocol.urls.add(baseUrl to config.handler)

    config.aliasRules.forEach { (aliasUrl, targetUrl) ->
        val target = "$baseUrl/${targetUrl.trimStart('/')}"
        val alias = "$baseUrl/${aliasUrl.trimStart('/')}"

        alias(alias, target)
    }

    protocol.urls.sortByDescending { (url, _) ->
        url.let {
            if (!it.endsWith("/")){
                "$it/"
            } else {
                it
            }
        }.count { it == '/' }
    }
}