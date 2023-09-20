package ru.compprehension.its.model

interface Info<Key, Value> : Map.Entry<Key, Value> {
    override val key: Key
    override val value: Value
}

open class InfoMap<I : Info<K, V>, K, V>(val info: Set<I>) : Map<K, V> {

    override val entries: Set<Map.Entry<K, V>>
        get() = info
    override val keys: Set<K>
        get() = info.map { it.key }.toSet()
    override val size: Int
        get() = info.size
    override val values: Collection<V>
        get() = info.map { it.value }.toSet()

    override fun containsKey(key: K): Boolean {
        return info.any { it.key == key }
    }

    override fun containsValue(value: V): Boolean {
        return info.any { it.value == value }
    }

    override fun get(key: K): V? {
        return info.firstOrNull { it.key == key }?.value
    }

    fun getFull(key: K): I? {
        return info.firstOrNull { it.key == key }
    }

    override fun isEmpty(): Boolean {
        return info.isEmpty()
    }
}

fun <V> V?.nullCheck(message: String): V {
    if (this == null) throw IllegalArgumentException(message)
    return this
}