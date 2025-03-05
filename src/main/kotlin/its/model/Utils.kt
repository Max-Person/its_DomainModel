package its.model

import java.net.URL
import java.util.*

/**
 * Общие вспомогательные функции
 */
object Utils {

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

    fun <V> getCollectionsDifference(first: Collection<V>, second: Collection<V>): CollectionsDifference<V> {
        val all = first.toMutableSet().also { it.addAll(second) }
        val onlyInFirst = mutableSetOf<V>()
        val inBoth = mutableSetOf<V>()
        val onlyInSecond = mutableSetOf<V>()
        for (item in all) {
            val inFirst = first.contains(item)
            val inSecond = second.contains(item)
            if (inFirst && inSecond) {
                inBoth.add(item)
            } else if (inFirst) {
                onlyInFirst.add(item)
            } else {
                onlyInSecond.add(item)
            }
        }
        return CollectionsDifference(onlyInFirst, inBoth, onlyInSecond)
    }

    fun <A, B> Pair<A, B>.toEntry(): Map.Entry<A, B> {
        return AbstractMap.SimpleImmutableEntry(first, second)
    }
}

data class CollectionsDifference<V>(
    val onlyInFirst: Set<V>,
    val inBoth: Set<V>,
    val onlyInSecond: Set<V>
)

/**
 * Кортеж значений
 */
class ValueTuple() : MutableList<Any?> by mutableListOf() {
    constructor(other: List<Any?>) : this() {
        this.addAll(other)
    }

    /**
     * "Подходит" ли данный кортеж к другому [other], с учетом того, что null в данном кортеже
     * считается подходящим любому элементу в другом кортеже (например, (*, 2) подходит к (1, 2) и (2, 2) и т.п.)
     */
    fun matches(other: List<*>): Boolean {
        if (this.size != other.size) return false
        return this.mapIndexed { i, el -> el == null || el == other[i] }
            .all { isTrue -> isTrue }
    }

    override fun toString(): String {
        return "(${this.map { it ?: "*" }.joinToString(";")})"
    }
}