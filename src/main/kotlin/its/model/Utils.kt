package its.model

import java.net.URL

/**
 * Общие вспомогательные функции
 */
object Utils {

    /**
     * элиас для проверки на ненулловость
     */
    val Any?.isPresent
        get() = this != null

    /**
     * элиас для проверки на нулловость
     */
    val Any?.isEmpty
        get() = this == null

    fun <V> V?.nullCheck(message: String): V {
        if (this == null) throw IllegalArgumentException(message)
        return this
    }

    @JvmStatic
    operator fun URL.plus(s: String): URL {
        return URL(
            this.protocol,
            this.host,
            this.port,
            this.path + (if (this.path.endsWith("/")) "" else "/") + s + (if (this.query.isNullOrBlank()) "" else "?" + this.query),
            null
        )
    }

    internal fun <T> List<T>.permutations(): List<List<T>> {
        return if (isEmpty()) listOf(emptyList())
        else mutableListOf<List<T>>().also { result ->
            for (i in this.indices) {
                (this - this[i]).permutations().forEach {
                    result.add(it + this[i])
                }
            }
        }
    }
}