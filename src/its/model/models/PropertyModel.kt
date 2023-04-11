package its.model.models

import its.model.expressions.types.EnumValue
import kotlin.reflect.KClass

/**
 * Модель свойства в предметной области
 * @param name Имя свойства
 * @param dataType Тип данных свойства
 * @param enumName Имя перечисления (только для свойств с типом Enum)
 * @param owners Имена классов, обладающих этим свойством
 * @param valueRange Диапазоны возможных значений свойства (только для свойств с типом Integer и Double)
 * @see DataType
 */
open class PropertyModel(
    val name: String,
    val dataType: KClass<*>,
    val enumName: String? = null,
    val isStatic: Boolean,
    val owners: List<String>? = null,
    val valueRange: Range? = null
) {

    /**
     * Проверяет корректность модели
     * @throws IllegalArgumentException
     */
    open fun validate() {
        require(name.isNotBlank()) {
            "Некорректное имя свойства."
        }
        require(
            dataType == Int::class
                    || dataType == Double::class
                    || dataType == Boolean::class
                    || dataType == String::class
                    || dataType == EnumValue::class
        ) {
            "Некорректный тип свойства $name."
        }
        require(owners == null || owners.isNotEmpty()) {
            "Свойством $name не обладает ни один класс."
        }
        require(dataType == Int::class || dataType == Double::class || valueRange == null) {
            "У свойства $name не может быть диапазонов значений, т.к. оно имеет тип $dataType."
        }
    }

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param value Значение
     */
    fun isValueInRange(value: Int): Boolean {
        if (dataType != Int::class) return false
        if (valueRange == null) return true

        return valueRange.contains(value)
    }

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param value Значение
     */
    fun isValueInRange(value: Double): Boolean {
        if (dataType != Double::class) return false
        if (valueRange == null) return true

        return valueRange.contains(value)
    }
}