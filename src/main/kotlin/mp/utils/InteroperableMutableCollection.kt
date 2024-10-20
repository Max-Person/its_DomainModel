package mp.utils

/**
 * Реализация [MutableCollection], скрывающая/переопределяющая методы [remove] и [contains],
 * которые на JVM принимают Object ([Any]), из-за чего плохо ладят с дженериками в котлине
 */
interface InteroperableMutableCollection<E> : MutableCollection<E> {
    override fun remove(element: E): Boolean {
        return removeElement(element)
    }

    /**
     * @see remove
     */
    fun removeElement(element: Any?): Boolean

    override fun contains(element: E): Boolean {
        return containsElement(element)
    }

    /**
     * @see contains
     */
    fun containsElement(element: Any?): Boolean
}