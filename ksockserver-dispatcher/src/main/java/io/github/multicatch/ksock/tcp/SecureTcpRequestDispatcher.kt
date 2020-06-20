package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import java.net.Socket
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.*
import java.util.concurrent.ExecutorService
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext


class SecureTcpRequestDispatcher<T : TcpProtocolProcessor>(
        private val certificate: CertificateWithKey,
        private val server: TcpServerConfiguration<T>,
        private val executor: ExecutorService
) : RequestDispatcher {

    private fun keyStoreOf(certificate: Certificate, privateKey: PrivateKey, password: String) = KeyStore
            .getInstance(KeyStore.getDefaultType())
            .also {
                it.load(null, password.toCharArray())
                it.setCertificateEntry("server", certificate)
                val chain = arrayOf(certificate)
                it.setKeyEntry("server", privateKey, password.toCharArray(), chain)
            }

    private fun keyManagerFactoryOf(keyStore: KeyStore, password: String) =
            KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm())
                    .also {
                        it.init(keyStore, password.toCharArray())
                    }

    override fun start() {
        executor.execute {
            val password = UUID.randomUUID().toString()
            val keyStore = keyStoreOf(certificate.certificate, certificate.key, password)
            val keyManagerFactory = keyManagerFactoryOf(keyStore, password)

            val context = SSLContext.getInstance("TLS")
            context.init(keyManagerFactory.keyManagers, null, null)
            context.serverSocketFactory
                    .createServerSocket(server.port)
                    .use {
                        while (true) {
                            dispatch(it.accept())
                        }
                    }
        }
    }

    private fun dispatch(acceptedSocket: Socket) {
        executor.execute(dispatcherOf(acceptedSocket, server.protocol))
    }

    override fun stop() {
        executor.shutdownNow()
    }
}