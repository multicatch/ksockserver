package io.github.multicatch.ksock.http

data class PlaintextHttpResponse(
        override val status: HttpStatus,
        val originalHeaders: Map<String, String>,
        val textEntity: String
) : HttpResponse {
    override val entity: ByteArray
        get() = textEntity.replace("\n", "\r\n").toByteArray()

    override val headers: Map<String, String>
        get() = originalHeaders
                .toMutableMap()
                .also { headers ->
                    headers["content-length"] = "${textEntity.length}"
                }
}

interface HttpResponse {
    val status: HttpStatus
    val headers: Map<String, String>
    val entity: ByteArray
}

interface HttpStatus {
    val code: Int
    val description: String
}

enum class StandardHttpStatus(
        override val code: Int,
        override val description: String
) : HttpStatus {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
}

internal data class NonStandardHttpStatus(
        override val code: Int,
        override val description: String
) : HttpStatus

fun httpStatusOf(code: Int, description: String? = null) =
        StandardHttpStatus.values().find { it.code == code }
                ?: NonStandardHttpStatus(code, description ?: "")

fun Int.toHttpStatus() = httpStatusOf(this)

fun String.extractHttpStatus(): HttpStatus {
    val firstResponseLine = split(" ")
    val statusCode = firstResponseLine[1].toInt()
    val statusDescription = firstResponseLine.drop(2).joinToString(" ")
    return httpStatusOf(statusCode, statusDescription)
}