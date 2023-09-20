package ru.compprehension.its.model.dictionaries

import ru.compprehension.its.model.models.ClassModel
import kotlin.reflect.KClass

/**
 * Словарь классов
 */
abstract class ClassesDictionaryBase<C : ClassModel>(storedType: KClass<C>) : DictionaryBase<C>(storedType) {

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    override fun onAddValidation(value: C) {
        require(value.name.isNotBlank()) {
            "Невозможно использовать пустое имя класса."
        }
        require(!exist(value.name)) {
            "Класс ${value.name} уже объявлено в словаре."
        }
    }

    override fun onAddActions(added: C) {}

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель класса по имени
     * @param name Имя класса
     */
    override fun get(name: String) = values.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    override fun validate() {
        values.forEach {
            it.validate()
            require(it.parent == null || exist(it.parent)) {
                "Класс ${it.parent} не объявлен в словаре."
            }
        }
    }

    /**
     * Существует ли класс
     * @param name Имя класса
     */
    fun exist(name: String) = values.any { it.name == name }

    /**
     * Получить имя родительского класса
     * @param name Имя класса
     */
    fun parent(name: String) = get(name)?.parent

    /**
     * Является ли класс родителем другого
     * @param child Ребенок
     * @param parent Родитель
     */
    fun isParentOf(child: String, parent: String): Boolean {
        if (!exist(child) || !exist(parent) || parent(child) == null) return false
        return if (parent(child) == parent) true else isParentOf(parent(child)!!, parent)
    }

    /**
     * Вычисляемый ли класс
     * @param name Имя класса
     */
    fun isCalculable(name: String) = get(name)?.calcExpr != null

    /**
     * Получить выражение для вычисления класса
     * @param name Имя класса
     * @return Выражение для вычисления
     */
    fun calcExpr(name: String) = get(name)?.calcExpr
}