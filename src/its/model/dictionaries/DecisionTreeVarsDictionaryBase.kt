package its.model.dictionaries

import its.model.DomainModel
import its.model.models.DecisionTreeVarModel
import kotlin.reflect.KClass

// TODO: вычислять по дереву
/**
 * Словарь переменных дерева мысли
 */
abstract class DecisionTreeVarsDictionaryBase<V : DecisionTreeVarModel>(storedType: KClass<V>) : DictionaryBase<V>(storedType) {

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    override fun onAddValidation(value: V) {
        require(!exist(value.name)) {
            "Переменная ${value.name} уже объявлено в словаре."
        }
        value.validate()
    }

    override fun onAddActions(added: V) {}

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель переменной по имени
     * @param name Имя переменной
     */
    override fun get(name: String) = values.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    override fun validate() {
        values.forEach {
            it.validate()
            require(DomainModel.classesDictionary.exist(it.className)) {
                "Класс ${it.className} не объявлен в словаре."
            }
        }
    }

    /**
     * Существует ли переменная с указанным именем
     * @param name Имя переменной
     */
    fun exist(name: String) = values.any { it.name == name }

    /**
     * Получить класс переменной дерева мысли
     * @param name Имя переменной
     * @return Имя класса переменной
     */
    fun getClass(name: String) = get(name)?.className
}