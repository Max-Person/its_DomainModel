package its.model.models

import its.model.expressions.types.DataType

/**
 * Модель свойства в предметной области
 * @param name Имя свойства
 * @param dataType Тип данных свойства
 * @param enumName Имя перечисления (только для свойств с типом Enum)
 * @param owners Имена классов, обладающих этим свойством
 * @param valuesRanges Диапазоны возможных значений свойства (только для свойств с типом Integer и Double)
 * @see DataType
 */
data class PropertyModel(
    val name: String,
    val dataType: DataType,
    val enumName: String? = null,
    val owners: List<String>? = null,
    val valuesRanges: List<Pair<Double, Double>>? = null
) {

    /**
     * Проверяет корректность модели
     * @throws IllegalArgumentException
     */
    fun validate() {
        require(name.isNotBlank()) {
            "Некорректное имя свойства."
        }
        require(
            dataType == DataType.Integer
                    || dataType == DataType.Double
                    || dataType == DataType.Boolean
                    || dataType == DataType.String
                    || dataType == DataType.Enum
        ) {
            "Некорректный тип свойства $name."
        }
        require(owners == null || owners.isNotEmpty()) {
            "Свойством $name не обладает ни один класс."
        }
        require(dataType == DataType.Integer || dataType == DataType.Double || valuesRanges == null) {
            "У свойства $name не может быть диапазонов значений, т.к. оно имеет тип $dataType."
        }
        valuesRanges?.forEach {
            require(it.first <= it.second) {
                "Начальное значение одного из диапазонов свойства $name больше конечного."
            }
        }
    }

    /**
     * Является свойство ли статическим
     */
    val isStatic
        get() = owners != null

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param value Значение
     */
    fun isValueInRange(value: Int): Boolean {
        if (dataType != DataType.Integer) return false
        if (valuesRanges.isNullOrEmpty()) return true

        valuesRanges.forEach {
            if (value.toDouble() in it.first..it.second) return true
        }
        return false
    }

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param value Значение
     */
    fun isValueInRange(value: Double): Boolean {
        if (dataType != DataType.Double) return false
        if (valuesRanges.isNullOrEmpty()) return true

        valuesRanges.forEach {
            if (value in it.first..it.second) return true
        }
        return false
    }
}