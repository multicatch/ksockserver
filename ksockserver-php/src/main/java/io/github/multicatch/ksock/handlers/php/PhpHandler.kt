package io.github.multicatch.ksock.handlers.php

import io.github.multicatch.ksock.http.*

fun HttpConfig.php(scriptRoot: String, cgi: String = "php-cgi") = apply {
    this.handler = { request ->
        val queryString = request.queryParams
                .entries
                .joinToString("&") { (key, value) ->
                    "$key=$value"
                }

        val process = ProcessBuilder(cgi)
                .apply {
                    environment().also { env ->
                        env["REQUEST_METHOD"] = request.method.name
                        env["SCRIPT_FILENAME"] = "$scriptRoot/${request.resourcePath}"
                        env["REDIRECT_STATUS"] = "CGI"
                        env["CONTENT_TYPE"] = "application/www-form-urlencoded"
                        env["REMOTE_ADDR"] = request.remoteAddress
                        env["SERVER_SOFTWARE"] = "ksockserver"
                        env["QUERY_STRING"] = queryString
                        env["CONTENT_LENGTH"] = "${request.entity.length}"
                        for ((key, value) in request.headers) {
                            env["HTTP_${key.replace("-", "_").toUpperCase()}"] = value
                        }
                    }
                }
                .redirectInput(ProcessBuilder.Redirect.PIPE)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .start()

        val bufferedWriter = process.outputStream.bufferedWriter()
        val bufferedReader = process.inputStream.bufferedReader()

        bufferedWriter.apply {
            write(request.entity)
            newLine()
            flush()
        }

        val response = bufferedReader.readText()
        val lines = response.split("\n")

        val headers = lines
                .asSequence()
                .extractHeaders()

        val status = headers["Status"]
                ?.split(" ")
                ?.first()
                ?.toInt()
                ?.toHttpStatus()
                ?: StandardHttpStatus.OK

        val responseEntity = lines.drop(lines.indexOfFirst { it.isBlank() })
                .joinToString("\n")

        PlaintextHttpResponse(
                status = status,
                originalHeaders = headers,
                stringEntity = responseEntity
        )
    }
}