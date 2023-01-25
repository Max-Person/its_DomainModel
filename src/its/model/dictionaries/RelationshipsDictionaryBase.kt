package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.dictionaries.DictionariesUtil.valueParser
import its.model.models.RelationshipModel
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaConstructor

/**
 * Словарь свойств
 */
abstract class RelationshipsDictionaryBase<R : RelationshipModel>(path: String, stored: KClass<R>) {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список отношений
     */
    private val relationships: MutableList<R> = mutableListOf()

    /**
     * Отношения порядковых шкал
     *
     * key - имя отношения,
     * val - имена отношений порядковых шкал для этого отношения
     */
    private val scaleRelationships: MutableMap<String, List<String>> = HashMap()

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    init {
        val parser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(parser).build()
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val constructor = stored.primaryConstructor?.javaConstructor!!
                val args = row.mapIndexed { index, s ->
                    valueParser.parseType(s, constructor.genericParameterTypes[index])
                }
                val relationship = constructor.newInstance(*args.toTypedArray())

                validateAdded(relationship)

                relationships.add(relationship)
                val scaleRelations = relationship.scaleRelationships().map {
                    require(stored.isInstance(it))
                    it as R
                }
                relationships.addAll(scaleRelations)
                scaleRelationships[relationship.name] = scaleRelations.map { it.name }
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    fun forEach(block: (R) -> Unit) {
        relationships.forEach(block)
    }

    /**
     * Получить модель отношения по имени
     * @param name Имя отношения
     */
    fun get(name: String) = relationships.firstOrNull { it.name == name }

    protected open fun validateAdded(added: R){
        require(!exist(added.name)) {
            "Отношение ${added.name} уже объявлено в словаре."
        }
        added.validate()
    }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    open fun validate() {
        relationships.forEach {
            it.validate()
            require(it.parent == null || exist(it.parent)) {
                "Отношение ${it.parent} не объявлено в словаре."
            }
            it.argsClasses.forEach { className ->
                require(ClassesDictionary.exist(className)) {
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
    fun exist(name: String) = relationships.any { it.name == name }

    /**
     * Получить список классов аргументов
     * @param name Имя отношения
     */
    fun args(name: String) = get(name)?.argsClasses
}