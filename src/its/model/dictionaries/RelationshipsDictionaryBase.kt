package its.model.dictionaries

import its.model.DomainModel
import its.model.models.RelationshipModel
import kotlin.reflect.KClass

/**
 * Словарь отношений
 */
abstract class RelationshipsDictionaryBase<R : RelationshipModel>(storedType: KClass<R>) : DictionaryBase<R>(storedType){

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Отношения порядковых шкал
     *
     * key - имя отношения,
     * val - имена отношений порядковых шкал для этого отношения
     */
    protected val scaleRelationships: MutableMap<String, List<String>> = HashMap()

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    override fun onAddValidation(value: R) {
        require(!exist(value.name)) {
            "Отношение ${value.name} уже объявлено в словаре."
        }
        value.validate()
    }

    override fun onAddActions(added: R) {
        val scaleRelations = added.scaleRelationships().map {
            require(storedType.isInstance(it))
            it as R
        }
        addAll(scaleRelations)
        scaleRelationships[added.name] = scaleRelations.map { it.name }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель отношения по имени
     * @param name Имя отношения
     */
    fun get(name: String) = values.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    override fun validate() {
        values.forEach {
            it.validate()
            require(it.parent == null || exist(it.parent)) {
                "Отношение ${it.parent} не объявлено в словаре."
            }
            it.argsClasses.forEach { className ->
                require(DomainModel.classesDictionary.exist(className)) {
                    "Класс $className не объявлен в словаре."
                }
            }
            require(
                it.scaleType == null
                        || scaleRelationships[it.name] != null
                        && scaleRelationships[it.name]?.size == 6
                        && exist(scaleRelationships[it.name]!![0])
                        && exist(scaleRelationships[it.name]!![1])
                        && exist(scaleRelationships[it.name]!![2])
                        && exist(scaleRelationships[it.name]!![3])
                        && exist(scaleRelationships[it.name]!![4])
                        && exist(scaleRelationships[it.name]!![5])
            ) {
                "Для отношения ${it.name} не указано одно из отношений порядковой шкалы или оно не объявлено в словаре."
            }
        }
    }

    /**
     * Существует ли отношение
     * @param name Имя отношения
     */
    fun exist(name: String) = values.any { it.name == name }

    /**
     * Получить список классов аргументов
     * @param name Имя отношения
     */
    fun args(name: String) = get(name)?.argsClasses
}