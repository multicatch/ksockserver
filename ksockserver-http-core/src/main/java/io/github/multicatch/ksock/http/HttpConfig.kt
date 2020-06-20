package io.github.multicatch.ksock.http

class HttpConfig {
    lateinit var handler: (HttpRequest) -> HttpResponse
    var aliasRules: MutableList<Pair<UrlPattern, String>> = mutableListOf()
}

interface UrlPattern {
    val basePath: String
    val specificity: Int
        get() = basePath.let {
            if (!it.endsWith("/")) {
                "$it/"
            } else {
                it
            }
        }.count { it == '/' }
    fun matches(path: String): Boolean

    fun trimBasePath(path: String): String = if (matches(path)) {
        path.drop(basePath.length)
    } else {
        path
    }
}

data class ExactUrl(
        override val basePath: String
) : UrlPattern {
    override fun matches(path: String): Boolean = path == basePath
}

data class IndexUrl(
        override val basePath: String
) : UrlPattern {
    override fun matches(path: String): Boolean = path.startsWith(basePath)
}

data class RelativeUrl(
        val baseUrl: UrlPattern,
        val extension: UrlPattern
) : UrlPattern {
    override val basePath: String by lazy {
        baseUrl.basePath.let {
            if (it.endsWith("/")) {
                it.dropLast(1)
            }
            if (!extension.basePath.startsWith("/")) {
                "$it/"
            } else {
                it
            }
        } + extension.basePath
    }

    override fun matches(path: String): Boolean = baseUrl.matches(path) && extension.matches(baseUrl.trimBasePath(path))
    override fun trimBasePath(path: String): String = extension.trimBasePath(baseUrl.trimBasePath(path))
}

fun exact(path: String): UrlPattern = ExactUrl(path)

fun index(path: String): UrlPattern = IndexUrl(path)