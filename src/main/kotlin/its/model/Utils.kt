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
}

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