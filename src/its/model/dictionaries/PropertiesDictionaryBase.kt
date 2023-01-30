package its.model.dictionaries

import its.model.DomainModel
import its.model.expressions.types.DataType
import its.model.models.PropertyModel
import kotlin.reflect.KClass

/**
 * Словарь свойств
 */
abstract class PropertiesDictionaryBase<P : PropertyModel>(storedType: KClass<P>) : DictionaryBase<P>(storedType) {

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    override fun onAddValidation(value: P) {
        require(!exist(value.name)) {
            "Свойство ${value.name} уже объявлено в словаре."
        }
        value.validate()
    }

    override fun onAddActions(added: P) {}

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель свойства по имени
     * @param name Имя свойства
     */
    override fun get(name: String) = values.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    override fun validate() {
        values.forEach {
            it.validate()
            require(it.dataType != DataType.Enum || it.enumName != null && DomainModel.enumsDictionary.exist(it.enumName)) {
                "Для свойства ${it.name} не указано имя перечисления, или оно не объявлено в словаре."
            }
            it.owners?.forEach { owner ->
                require(DomainModel.classesDictionary.exist(owner)) {
                    "Класс $owner не объявлен в словаре."
                }
            }
        }
    }

    /**
     * Существует ли свойство
     * @param name Имя свойства
     */
    fun exist(name: String) = values.any { it.name == name }

    /**
     * Является ли статическим
     * @param name Имя свойства
     */
    fun isStatic(name: String) = get(name)?.isStatic

    /**
     * Получить имя перечисления свойства
     * @param name Имя свойства
     * @return Имя перечисления
     */
    fun enumName(name: String) = get(name)?.enumName

    /**
     * Тип данных свойства
     * @param name Имя свойства
     */
    fun dataType(name: String) = get(name)?.dataType

    /**
     * Переопределяется ли свойство
     * @param name Имя свойства
     */
    fun isPropertyBeingOverridden(name: String): Boolean {
        if (!exist(name)) return false
        if (isStatic(name) != true) return false

        val classes = get(name)?.owners!!
        for (i in classes.indices) {
            for (j in classes.indices) {
                if (i == j) continue
                if (DomainModel.classesDictionary.isParentOf(classes[i], classes[j])) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param name Имя свойства
     * @param value Значение
     */
    fun isValueInRange(name: String, value: Int) = get(name)?.isValueInRange(value)

    /**
     * Попадает ли значение в один из диапазонов свойства
     * @param name Имя свойства
     * @param value Значение
     */
    fun isValueInRange(name: String, value: Double) = get(name)?.isValueInRange(value)
}