package io.github.multicatch.khttp

import io.github.multicatch.khttp.http.HttpResponse
import io.github.multicatch.khttp.http.HttpStatus
import java.net.Socket
import java.util.concurrent.ExecutorService

private val DEFAULT_RESPONSE = HttpResponse(
        status = HttpStatus.NOT_FOUND,
        headers = mapOf(
                "Content-Type" to "text/plain"
        ),
        entity = ""
)

class RequestDispatcher(
        private val serverConfiguration: ServerConfiguration,
        private val executor: ExecutorService
) {

    fun dispatch(acceptedSocket: Socket) {
        executor.execute(dispatcherOf(acceptedSocket))
    }

    private fun dispatcherOf(acceptedSocket: Socket): () -> Unit = {
        acceptedSocket.use {
            val request = it.getInputStream().readRequest(it.inetAddress.hostAddress)
            println(request)

            val handler = serverConfiguration.urls.getOrDefault(request.resourceUri) {
                DEFAULT_RESPONSE
            }

            val response = handler(request)

            it.getOutputStream().write(response)
        }
    }

    fun finish() {
        executor.shutdownNow()
    }
}
