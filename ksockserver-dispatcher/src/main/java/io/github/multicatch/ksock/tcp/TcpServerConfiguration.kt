package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import java.net.ServerSocket
import java.util.concurrent.Executors

fun <T : TcpProtocolProcessor> bindTcp(
        port: Int, protocol: T,
        maxThreads: Int = 10,
        configuration: TcpServerConfiguration<T>.() -> Unit
): RequestDispatcher {
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    return TcpRequestDispatcher(serverConfiguration, Executors.newFixedThreadPool(maxThreads))
}

class TcpServerConfiguration<T : TcpProtocolProcessor>(
        val port: Int = 0,
        val protocol: T
)
