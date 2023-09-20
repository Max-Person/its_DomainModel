package ru.compprehension.its.model.dictionaries

import ru.compprehension.its.model.expressions.types.EnumValue
import ru.compprehension.its.model.models.EnumModel
import kotlin.reflect.KClass

/**
 * Словарь перечислений
 */
abstract class EnumsDictionaryBase<E : EnumModel>(storedType: KClass<E>) : DictionaryBase<E>(storedType) {

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    override fun onAddValidation(value: E) {
        require(!exist(value.name)) {
            "Перечисление ${value.name} уже объявлено в словаре."
        }
        value.validate()
    }

    override fun onAddActions(added: E) {}

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель перечисления по имени
     * @param name Имя перечисления
     */
    override fun get(name: String) = values.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    override fun validate() {
        values.forEach {
            it.validate()
        }
    }

    /**
     * Существует ли перечисление
     * @param name Имя перечисления
     */
    fun exist(name: String) = values.any { it.name == name }

    /**
     * Содержит ли перечисление указанное значение
     * @param name Имя перечисления
     * @param value Значение
     */
    fun containsValue(name: String, value: String) = get(name)?.containsValue(value)
    fun containsValue(value: EnumValue) = containsValue(value.ownerEnum, value.value)

    /**
     * Получить список всех значений перечисления
     * @param name Имя перечисления
     * @return Список всех значений
     */
    fun values(name: String) = get(name)?.values
}