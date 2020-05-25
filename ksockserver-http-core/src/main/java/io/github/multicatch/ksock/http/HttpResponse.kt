package io.github.multicatch.ksock.http

data class HttpResponse(
        val status: HttpStatus,
        val headers: Map<String, String>,
        val entity: String
) {
    fun entityLength() = entity.length + entity.count { it == '\n' } * 2
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