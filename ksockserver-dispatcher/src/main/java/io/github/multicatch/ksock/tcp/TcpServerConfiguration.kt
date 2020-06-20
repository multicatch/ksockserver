package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import org.apache.logging.log4j.LogManager
import java.util.concurrent.Executors

fun <I, O, T : TcpProtocolProcessor<I, O>> bindTCP(
        port: Int,
        protocol: T,
        configuration: TcpServerConfiguration<I, O, T>.() -> Unit
): RequestDispatcher {
    logger.trace("Start of TCP configuration")
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    logger.trace("Creating a TCP Request Dispatcher...")
    return TcpRequestDispatcher(serverConfiguration)
}

fun <I, O, T : TcpProtocolProcessor<I, O>> bindSecureTCP(
        port: Int,
        protocol: T,
        serverCertificate: CertificateWithKey,
        configuration: TcpServerConfiguration<I, O, T>.() -> Unit
): RequestDispatcher {
    logger.trace("Start of Secure TCP configuration")
    val serverConfiguration = TcpServerConfiguration(
            port = port,
            protocol = protocol
    ).apply(configuration)

    logger.trace("Creating a Secure TCP Request Dispatcher...")
    return SecureTcpRequestDispatcher(
            serverCertificate,
            serverConfiguration
    )
}

class TcpServerConfiguration<I, O, T : TcpProtocolProcessor<I, O>>(
        val port: Int = 0,
        val protocol: T
)

private val logger = LogManager.getLogger(TcpServerConfiguration::class.java)
