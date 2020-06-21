package io.github.multicatch.ksock.task

import org.apache.logging.log4j.LogManager
import java.net.Socket
import java.util.concurrent.*

interface Task : Callable<Task> {
    override fun call() : Task?
    fun cleanup(): Task? = this
}

interface ConnectedTask : Task {
    val socket: Socket
    val semaphore: Semaphore

    override fun call(): Task? {
        logger.trace("Releasing keep-alive lock of ${socket.inetAddress.hostAddress}.")
        semaphore.release()
        logger.trace("Checking whether connection with ${socket.inetAddress.hostAddress} is still open.")
        if (socket.isConnected && !socket.isClosed) {
            logger.trace("Connection with ${socket.inetAddress.hostAddress} is open, running task.")
            val result = try {
                runOnConnected()
            } catch (throwable: Throwable) {
                logger.error("Got an error while running a task on a socket, aborting", throwable)
                null
            }
            if (result != null) {
                logger.debug("Connection with ${socket.inetAddress.hostAddress} is kept alive because of a running task")
                semaphore.acquire()
            }
            return result
        }
        return null
    }

    fun runOnConnected(): Task?
}

fun ExecutorService.runWithTimeout(task: Task, timeout: Long, unit: TimeUnit): Task? {
    return try {
        logger.trace("Running task ${task::class.java.canonicalName} with timeout ${timeout} ${unit}")
        submit {
            task.call()
        }.get(timeout, unit) as Task?
    } catch (timeout: TimeoutException) {
        logger.debug("Task ${task::class.java.canonicalName} interrupted because it was running too long", timeout)
        task.cleanup()
    }
}

private val logger = LogManager.getLogger(Task::class.java)