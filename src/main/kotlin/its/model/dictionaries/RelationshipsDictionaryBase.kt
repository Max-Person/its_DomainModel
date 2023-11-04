package its.model.dictionaries

import its.model.DomainModel
import its.model.models.RelationshipModel
import kotlin.reflect.KClass

/**
 * Словарь отношений
 */
abstract class RelationshipsDictionaryBase<R : RelationshipModel>(storedType: KClass<R>) :
    DictionaryBase<R>(storedType) {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Отношения порядковых шкал
     *
     * key - имя отношения,
     * val - имена отношений порядковых шкал для этого отношения
     */
    protected val scaleRelationships: MutableMap<String, Map<RelationshipModel.ScaleRole, String>> = HashMap()

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
            require(storedType.isInstance(it)) {
                "Отношения типа ${storedType.simpleName}, задающие порядковую шкалу, должны создавать отношения того же типа как производные.\n" +
                        "Скорее всего функция scaleRelationships() не переопределена в классе $storedType."
            }
            it as R
        }
        addAll(scaleRelations)
        scaleRelationships[added.name] = scaleRelations.map { it.scaleRole!! to it.name }.toMap()
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Получить модель отношения по имени
     * @param name Имя отношения
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
                "Отношение ${it.parent} не объявлено в словаре."
            }
            it.argsClasses.forEach { className ->
                require(DomainModel.classesDictionary.exist(className)) {
                    "Класс $className не объявлен в словаре."
                }
            }
            require(
                it.scaleType == null
                        || it.scaleRole != RelationshipModel.ScaleRole.Base
                        || scaleRelationships[it.name]?.size == 6
                        && exist(scaleRelationships[it.name]!![RelationshipModel.ScaleRole.Reverse]!!)
                        && exist(scaleRelationships[it.name]!![RelationshipModel.ScaleRole.BaseTransitive]!!)
                        && exist(scaleRelationships[it.name]!![RelationshipModel.ScaleRole.ReverseTransitive]!!)
                        && exist(scaleRelationships[it.name]!![RelationshipModel.ScaleRole.Between]!!)
                        && exist(scaleRelationships[it.name]!![RelationshipModel.ScaleRole.Closer]!!)
                        && exist(scaleRelationships[it.name]!![RelationshipModel.ScaleRole.Further]!!)
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