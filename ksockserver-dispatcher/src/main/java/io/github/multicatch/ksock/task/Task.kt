package io.github.multicatch.ksock.task

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
        semaphore.release()
        if (socket.isConnected && !socket.isClosed) {
            val result = try {
                runOnConnected()
            } catch (throwable: Throwable) {
                null
            }
            if (result != null) {
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
        submit {
            task.call()
        }.get(timeout, unit) as Task?
    } catch (timeout: TimeoutException) {
        timeout.printStackTrace()
        timeout.cause?.printStackTrace()
        task.cleanup()
    }
}