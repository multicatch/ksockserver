package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ExecutorService
import javax.net.ssl.SSLServerSocket

class TcpRequestDispatcher<T : TcpProtocolProcessor>(
        private val server: TcpServerConfiguration<T>,
        private val executor: ExecutorService
) : RequestDispatcher {

    override fun start() {
        executor.execute {
            ServerSocket(server.port).use {
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

fun <T : TcpProtocolProcessor> dispatcherOf(acceptedSocket: Socket, protocol: T): () -> Unit = {
    acceptedSocket.use {
        protocol.process(connection = it)
    }
}
