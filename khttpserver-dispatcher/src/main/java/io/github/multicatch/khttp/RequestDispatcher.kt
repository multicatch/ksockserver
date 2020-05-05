package io.github.multicatch.khttp

import java.net.Socket
import java.util.concurrent.ExecutorService


class RequestDispatcher<T : ServerProtocolProcessor>(
        private val server: ServerConfiguration<T>,
        private val executor: ExecutorService
) {

    fun dispatch(acceptedSocket: Socket) {
        executor.execute(dispatcherOf(acceptedSocket))
    }

    private fun dispatcherOf(acceptedSocket: Socket): () -> Unit = {
        acceptedSocket.use {
            server.protocol.process(connection = it)
        }
    }

    fun finish() {
        executor.shutdownNow()
    }
}
