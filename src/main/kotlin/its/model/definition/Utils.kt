package its.model.definition

import java.net.URL

/**
 * Общие вспомогательные функции
 */
internal object Utils {

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

    fun <T> List<T>.permutations(): List<List<T>> {
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