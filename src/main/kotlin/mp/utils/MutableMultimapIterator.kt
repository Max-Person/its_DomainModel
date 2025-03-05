package mp.utils

/**
 * Вспомогательный класс итератор для реализации мультимап
 */
abstract class MutableMultimapIterator<V>(multimap: MutableMap<*, out MutableCollection<V>>) : MutableIterator<V> {
    private val iterator: MutableIterator<V>
    private var lastValue: V? = null

    init {
        iterator = multimap.values.flatMap { it.toList() }.toMutableList().listIterator()
    }


    override fun hasNext(): Boolean {
        return iterator.hasNext()
    }

    override fun next(): V {
        lastValue = iterator.next()
        return lastValue!!
    }

    override fun remove() {
        if (lastValue != null) {
            remove(lastValue!!)
        }
    }

    protected abstract fun remove(value: V)
}