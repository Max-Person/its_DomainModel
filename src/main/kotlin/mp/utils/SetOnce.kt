package mp.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Делегат для свойства, позволяющий установить значение только однажды (повторные установки значения не имеют эффекта)
 * Попытка прочитать значение свойства до его установки выбрасывает исключение
 */
class SetOnce<T> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value ?: throw IllegalStateException("Property ${property.name} should be initialized before get.")
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this.value != null) return
        this.value = value
    }

    override fun toString(): String =
        "LateinitVal(${if (value != null) "value=$value" else "value not initialized yet"})"

}