package io.github.multicatch.ksock.tcp

import com.sun.security.ntlm.Server
import io.github.multicatch.ksock.RequestDispatcher
import io.github.multicatch.ksock.task.ConnectedTask
import io.github.multicatch.ksock.task.Task
import io.github.multicatch.ksock.task.runWithTimeout
import org.apache.logging.log4j.LogManager
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.*

class TcpRequestDispatcher<I, O, T : TcpProtocolProcessor<I, O>>(
        private val server: TcpServerConfiguration<I, O, T>,
        private val taskTimeout: Long = 100,
        private val executor: ExecutorService = Executors.newFixedThreadPool(10),
        private val taskDeque: LinkedBlockingDeque<Task> = LinkedBlockingDeque()
) : RequestDispatcher {

    override fun start() {
        logger.debug("Starting the TCP Request Dispatcher on port ${server.port}. Using protocol ${server.protocol::class.java.canonicalName}")
        executor.execute {
            ServerSocket(server.port).listenForConnections(server.protocol, taskDeque)
        }
        executor.handleQueue(taskDeque, taskTimeout)
        logger.debug("TCP Request Dispatcher of ${server.port} started.")
    }

    override fun stop() {
        logger.info("Stopping the TCP Request Dispatcher of ${server.port}.")
        executor.shutdownNow()
    }
}

fun <I, O, T : TcpProtocolProcessor<I, O>> ServerSocket.listenForConnections(protocol: T, taskDeque: LinkedBlockingDeque<Task>) = use {
    logger.info("Server started on ${it.localPort}.")
    while (true) {
        logger.info("Got new connection from ${it.inetAddress.hostAddress}, using ${protocol::class.java.canonicalName}")
        dispatch(it.accept(), protocol, taskDeque)
    }
}

fun <I, O, T : TcpProtocolProcessor<I, O>> dispatch(acceptedSocket: Socket, protocol: T, taskDeque: LinkedBlockingDeque<Task>) {
    createTasks(acceptedSocket, protocol)
            .forEach {
                logger.trace("Adding task ${it::class.java.canonicalName} of ${acceptedSocket.inetAddress.hostAddress}.")
                taskDeque.offer(it)
            }
}

fun ExecutorService.handleQueue(taskDeque: LinkedBlockingDeque<Task>, timeout: Long) {
    execute {
        val helperExecutor = Executors.newSingleThreadExecutor()
        while (true) {
            if (taskDeque.isNotEmpty()) {
                val task = taskDeque.pop()
                try {
                    val newTask = helperExecutor.runWithTimeout(task, timeout, TimeUnit.MILLISECONDS)
                    if (newTask != null) {
                        taskDeque.offer(newTask)
                    }
                } catch (throwable: Throwable) {
                    logger.error("Got an error while processing task, retrying", throwable)
                    taskDeque.offer(task)
                }
            }
        }
    }
}

fun <I, O, T : TcpProtocolProcessor<I, O>> createTasks(acceptedSocket: Socket, protocol: T): List<Task> {
    logger.debug("Received new connection from ${acceptedSocket.inetAddress.hostAddress}, preparing tasks...")

    val reader = protocol.reader(acceptedSocket)
    val writer = protocol.writer(acceptedSocket)
    val incomingQueue = LinkedBlockingDeque<I>()
    val outgoingQueue = LinkedBlockingDeque<O>()
    val processor = protocol.processor(outgoingQueue, acceptedSocket)
    val semaphore = Semaphore(3)

    val result = listOf(
            ReaderTask(acceptedSocket, incomingQueue, reader, semaphore),
            ProcessingTask(acceptedSocket, incomingQueue, processor, semaphore),
            WriterTask(acceptedSocket, outgoingQueue, writer, semaphore),
            ConnectionCloseTask(acceptedSocket, semaphore)
    )
    logger.debug("Tasks for ${acceptedSocket.inetAddress.hostAddress} prepared.")
    return result
}

class ReaderTask<I>(
        override val socket: Socket,
        private val incomingQueue: LinkedBlockingDeque<I>,
        private val reader: MessageReader<I>,
        override val semaphore: Semaphore
) : ConnectedTask {
    override fun runOnConnected(): Task? {
        logger.trace("Reading from ${socket.inetAddress.hostAddress}")
        val message = reader.read()
        if (message != null) {
            logger.trace("Adding a new message from ${socket.inetAddress.hostAddress} to incoming queue")
            incomingQueue.offer(message)
        }
        return null
    }

    override fun cleanup(): Task? {
        reader.interrupt()
        return super.cleanup()
    }
}

class WriterTask<O>(
        override val socket: Socket,
        private val outgoingQueue: LinkedBlockingDeque<O>,
        private val writer: MessageWriter<O>,
        override val semaphore: Semaphore
) : ConnectedTask {
    override fun runOnConnected(): Task? {
        logger.trace("Writing to ${socket.inetAddress.hostAddress}")
        writer.resume()
        if (outgoingQueue.isNotEmpty()) {
            logger.trace("Adding a new message ${socket.inetAddress.hostAddress} to outgoing queue")
            writer.write(outgoingQueue.pop())
        }
        return null
    }

    override fun cleanup(): Task? {
        writer.interrupt()
        return super.cleanup()
    }
}

class ProcessingTask<I, O>(
        override val socket: Socket,
        private val incomingQueue: LinkedBlockingDeque<I>,
        private val processor: MessageProcessor<I, O>,
        override val semaphore: Semaphore
) : ConnectedTask {
    override fun runOnConnected(): Task? {
        logger.trace("Processing incoming queue of ${socket.inetAddress.hostAddress}")
        processor.resume()
        processor.process(incomingQueue.pop())
        return null
    }

    override fun cleanup(): Task? {
        processor.interrupt()
        return super.cleanup()
    }
}

class ConnectionCloseTask(
        private val socket: Socket,
        private val semaphore: Semaphore
) : Task {
    override fun call(): Task? {
        logger.trace("Testing ${socket.inetAddress.hostAddress} connection")
        if (semaphore.availablePermits() == 3) {
            logger.debug("Closing connection with ${socket.inetAddress.hostAddress}")
            socket.getInputStream().close()
            socket.getOutputStream().close()
            socket.close()
            logger.info("Closing with ${socket.inetAddress.hostAddress} closed.")
            return null
        }
        return this
    }
}

private val logger = LogManager.getLogger(TcpRequestDispatcher::class.java)