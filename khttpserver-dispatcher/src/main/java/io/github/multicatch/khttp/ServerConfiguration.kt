package io.github.multicatch.khttp

import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

fun <T : ServerProtocolProcessor> bind(port: Int, protocol: T, configuration: ServerConfiguration<T>.() -> Unit) {
    val serverConfiguration = ServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    val dispatcher = RequestDispatcher(serverConfiguration, Executors.newFixedThreadPool(10))

    ServerSocket(serverConfiguration.port).use {
        while(true) {
            dispatcher.dispatch(it.accept())
        }
    }
}

class  ServerConfiguration<T : ServerProtocolProcessor>(
        val port: Int = 0,
        val protocol: T
)

interface ServerProtocolProcessor {
    fun process(connection: Socket)
}