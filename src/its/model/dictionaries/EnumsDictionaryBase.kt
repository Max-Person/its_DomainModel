package its.model.dictionaries

import its.model.models.EnumModel
import kotlin.reflect.KClass

/**
 * Словарь перечислений
 */
abstract class EnumsDictionaryBase<E : EnumModel>(path: String, storedType: KClass<E>) : DictionaryBase<E>(path, storedType)  {

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    override fun onAddValidation(value: E, stored: KClass<E>) {
        require(!exist(value.name)) {
            "Перечисление ${value.name} уже объявлено в словаре."
        }
        value.validate()
    }

    override fun onAddActions(added: E, stored: KClass<E>) {}

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель перечисления по имени
     * @param name Имя перечисления
     */
    internal fun get(name: String) = values.firstOrNull { it.name == name }

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

    /**
     * Получить список всех значений перечисления
     * @param name Имя перечисления
     * @return Список всех значений
     */
    fun values(name: String) = get(name)?.values
}