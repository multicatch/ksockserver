package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import io.github.multicatch.ksock.task.Task
import java.net.Socket
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.Certificate
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingDeque
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext


class SecureTcpRequestDispatcher<I, O, T : TcpProtocolProcessor<I, O>>(
        private val certificate: CertificateWithKey,
        private val server: TcpServerConfiguration<I, O, T>,
        private val taskTimeout: Long = 100,
        private val executor: ExecutorService = Executors.newFixedThreadPool(10),
        private val taskDeque: LinkedBlockingDeque<Task> = LinkedBlockingDeque()
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
        executor.handleQueue(taskDeque, taskTimeout)
    }

    private fun dispatch(acceptedSocket: Socket) {
        createTasks(acceptedSocket, server.protocol)
                .forEach {
                    taskDeque.offer(it)
                }
    }

    override fun stop() {
        executor.shutdownNow()
    }
}