package io.github.multicatch.ksock.tcp

import java.net.Socket

interface TcpProtocolProcessor {
    fun process(connection: Socket)
}