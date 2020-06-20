package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import java.util.concurrent.Executors

fun <T : TcpProtocolProcessor> bindTCP(
        port: Int,
        protocol: T,
        maxThreads: Int = 10,
        configuration: TcpServerConfiguration<T>.() -> Unit
): RequestDispatcher {
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    return TcpRequestDispatcher(serverConfiguration, Executors.newFixedThreadPool(maxThreads))
}

fun <T : TcpProtocolProcessor> bindSecureTCP(
        port: Int,
        protocol: T,
        maxThreads: Int = 10,
        serverCertificate: CertificateWithKey,
        configuration: TcpServerConfiguration<T>.() -> Unit
): RequestDispatcher {
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    return SecureTcpRequestDispatcher(
            serverCertificate,
            serverConfiguration,
            Executors.newFixedThreadPool(maxThreads)
    )
}

class TcpServerConfiguration<T : TcpProtocolProcessor>(
        val port: Int = 0,
        val protocol: T
)
