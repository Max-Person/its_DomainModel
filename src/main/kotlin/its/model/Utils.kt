package its.model

import java.net.URL

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
}

data class CollectionsDifference<V>(
    val onlyInFirst: Set<V>,
    val inBoth: Set<V>,
    val onlyInSecond: Set<V>
)

abstract class Association<K, V> : MutableCollection<V> {

    constructor()

    constructor(elements: Collection<V>) {
        this.addAll(elements)
    }

    private val map: MutableMap<K, V> = mutableMapOf()
    override val size: Int
        get() = map.size

    override fun clear() = map.clear()
    override fun isEmpty() = map.isEmpty()
    override fun containsAll(elements: Collection<V>) = map.values.containsAll(elements)
    override fun contains(element: V) = map.containsValue(element)
    fun containsKey(key: K) = map.containsKey(key)
    operator fun get(key: K): V? = map[key]
    fun removeByKey(key: K): V? = map.remove(key)
    val keys: Set<K>
        get() = map.keys


    override fun iterator(): MutableIterator<V> = AssociationIterator(this)
    private class AssociationIterator<K, V>(assoc: Association<K, V>) : MutableIterator<V> {
        val iter = assoc.map.iterator()
        override fun hasNext() = iter.hasNext()
        override fun next() = iter.next().value
        override fun remove() = iter.remove()
    }

    protected abstract fun getKey(element: V): K

    protected open val supportsOverwrites: Boolean = true

    override fun add(element: V): Boolean {
        val key = getKey(element)
        if (map.containsKey(key) && !supportsOverwrites) {
            return false
        }
        map[key] = element
        return true
    }

    override fun remove(element: V): Boolean {
        val key = getKey(element)
        if (!map.containsKey(key)) {
            return false
        }
        return map.remove(key, element)
    }

    override fun addAll(elements: Collection<V>): Boolean {
        var added = false
        elements.forEach { added = add(it) || added }
        return added
    }

    override fun retainAll(elements: Collection<V>): Boolean {
        var removed = false
        this.forEach { if (!elements.contains(it)) removed = remove(it) || removed }
        return removed
    }

    override fun removeAll(elements: Collection<V>): Boolean {
        var removed = false
        elements.forEach { removed = remove(it) || removed }
        return removed
    }
}

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