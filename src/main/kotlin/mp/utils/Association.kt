package mp.utils

/**
 * Ассоциация - гибрид списка и мапы.
 * Содержит упорядоченный набор значений [V], для каждого из которых с помощью метода [getKey] также определяется ключ [K].
 * Ассоциация поддерживает как получение по индексу, так и получение по ключу.
 */
abstract class Association<K, V> : ArrayList<V> {

    constructor()

    constructor(elements: Collection<V>) {
        this.addAll(elements)
    }

    private val map: MutableMap<K, V> = mutableMapOf()

    override fun clear() {
        super.clear()
        map.clear()
    }

    fun containsKey(key: K) = map.containsKey(key)
    operator fun get(key: K): V? = map[key]
    fun removeByKey(key: K): V? = map.remove(key)
    val keys: Set<K>
        get() = map.keys

    protected abstract fun getKey(element: V): K
    protected open val supportsOverwrites: Boolean = true

    override fun add(element: V): Boolean {
        val key = getKey(element)
        if (map.containsKey(key) && !supportsOverwrites) {
            return false
        }
        if (super.add(element)) {
            map[key] = element
            return true
        }
        return false
    }

    override fun remove(element: V): Boolean {
        val key = getKey(element)
        if (!map.containsKey(key)) {
            return false
        }
        if (super.remove(element)) {
            return map.remove(key, element)
        }
        return false
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
