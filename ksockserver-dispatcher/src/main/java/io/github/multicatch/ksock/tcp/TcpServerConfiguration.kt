package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import java.util.concurrent.Executors

fun <I, O, T : TcpProtocolProcessor<I, O>> bindTCP(
        port: Int,
        protocol: T,
        configuration: TcpServerConfiguration<I, O, T>.() -> Unit
): RequestDispatcher {
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    return TcpRequestDispatcher(serverConfiguration)
}

fun <I, O, T : TcpProtocolProcessor<I, O>> bindSecureTCP(
        port: Int,
        protocol: T,
        serverCertificate: CertificateWithKey,
        configuration: TcpServerConfiguration<I, O, T>.() -> Unit
): RequestDispatcher {
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    return SecureTcpRequestDispatcher(
            serverCertificate,
            serverConfiguration
    )
}

class TcpServerConfiguration<I, O, T : TcpProtocolProcessor<I, O>>(
        val port: Int = 0,
        val protocol: T
)
