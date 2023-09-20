package ru.compprehension.its.model.models

/**
 * Модель перечисления в предметной области
 * @param name Имя перечисления
 * @param values Возможные значения
 * @param isLinear Является ли перечисление упорядоченным
 */
open class EnumModel(
    val name: String,
    val values: List<String>,
    val isLinear: Boolean,
) {

    /**
     * Проверяет корректность модели
     * @throws IllegalArgumentException
     */
    open fun validate() {
        require(name.isNotBlank()) {
            "Некорректное имя перечисления."
        }
        require(values.isNotEmpty()) {
            "Перечисление $name не содержит значений."
        }
    }

    /**
     * Содержит ли перечисление указанное значение
     * @param value Значение
     */
    fun containsValue(value: String) = values.contains(value)
}