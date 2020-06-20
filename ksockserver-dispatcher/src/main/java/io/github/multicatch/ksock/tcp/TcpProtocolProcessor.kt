package io.github.multicatch.ksock.tcp

import java.net.Socket
import java.util.concurrent.LinkedBlockingDeque

interface TcpProtocolProcessor<I, O> {
    fun reader(connection: Socket): MessageReader<I>
    fun writer(connection: Socket): MessageWriter<O>
    fun processor(outgoing: LinkedBlockingDeque<O>, socket: Socket): MessageProcessor<I, O>
}

interface InterruptibleProtocolTask {
    fun resume() {
        // do nothing by default
    }

    fun interrupt() {
        // do nothing by default
    }
}

interface MessageReader<M> : InterruptibleProtocolTask {
    fun read(): M?
}

interface MessageWriter<M> : InterruptibleProtocolTask {
    fun write(message: M)
}

interface MessageProcessor<I, O> : InterruptibleProtocolTask {
    fun process(message: I)
}