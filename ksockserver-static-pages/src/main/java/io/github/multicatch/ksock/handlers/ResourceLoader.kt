package io.github.multicatch.ksock.handlers

import java.io.File

private const val CLASSPATH_PREFIX = "classpath:"

fun String.load() = if (this.startsWith(CLASSPATH_PREFIX)) {
        Thread.currentThread()
                .contextClassLoader
                .getResourceAsStream(this.drop(CLASSPATH_PREFIX.length)
                        .trimStart('/')
                )
    } else {
        File(this).takeIf { it.exists() }
                ?.inputStream()
    }