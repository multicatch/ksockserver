package io.github.multicatch.ksock.tcp

import io.github.multicatch.ksock.RequestDispatcher
import io.github.multicatch.ksock.task.ConnectedTask
import io.github.multicatch.ksock.task.Task
import io.github.multicatch.ksock.task.runWithTimeout
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
        executor.execute {
            ServerSocket(server.port).use {
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
                    taskDeque.offer(task)
                }
            }
        }
    }
}

fun <I, O, T : TcpProtocolProcessor<I, O>> createTasks(acceptedSocket: Socket, protocol: T): List<Task> {
    val reader = protocol.reader(acceptedSocket)
    val writer = protocol.writer(acceptedSocket)
    val incomingQueue = LinkedBlockingDeque<I>()
    val outgoingQueue = LinkedBlockingDeque<O>()
    val processor = protocol.processor(outgoingQueue, acceptedSocket)
    val semaphore = Semaphore(3)

    return listOf(
            ReaderTask(acceptedSocket, incomingQueue, reader, semaphore),
            ProcessingTask(acceptedSocket, incomingQueue, processor, semaphore),
            WriterTask(acceptedSocket, outgoingQueue, writer, semaphore),
            ConnectionCloseTask(acceptedSocket, incomingQueue, outgoingQueue, semaphore)
    )
}

class ReaderTask<I>(
        override val socket: Socket,
        private val incomingQueue: LinkedBlockingDeque<I>,
        private val reader: MessageReader<I>,
        override val semaphore: Semaphore
) : ConnectedTask {
    override fun runOnConnected(): Task? {
        val message = reader.read()
        if (message != null) {
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
        writer.resume()
        if (outgoingQueue.isNotEmpty()) {
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
        processor.resume()
        processor.process(incomingQueue.pop())
        return null
    }

    override fun cleanup(): Task? {
        processor.interrupt()
        return super.cleanup()
    }
}

class ConnectionCloseTask<I, O>(
        private val socket: Socket,
        private val incomingQueue: LinkedBlockingDeque<I>,
        private val outgoingQueue: LinkedBlockingDeque<O>,
        private val semaphore: Semaphore
) : Task {
    override fun call(): Task? {
        if (semaphore.availablePermits() == 3) {
            socket.getInputStream().close()
            socket.getOutputStream().close()
            socket.close()
            return null
        }
        return this
    }
}