package io.github.multicatch.ksock.http

data class HttpResponse(
        val status: HttpStatus,
        val headers: Map<String, String>,
        val entity: String
) {
    fun entityLength() = entity.length + entity.count { it == '\n' } * 2
}

enum class HttpStatus(val code: Int, val description: String) {
    OK(200, "OK"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");
}