package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.dictionaries.DictionariesUtil.LIST_ITEMS_SEPARATOR
import its.model.models.RelationshipModel
import its.model.models.RelationshipModel.Companion.RelationType
import its.model.models.RelationshipModel.Companion.ScaleType
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Словарь свойств
 */
object RelationshipsDictionary {

    // +++++++++++++++++++++++++++++++++ Свойства ++++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Список отношений
     */
    private val relationships: MutableList<RelationshipModel> = mutableListOf()

    /**
     * Отношения порядковых шкал
     *
     * key - имя отношения,
     * val - имена отношений порядковых шкал для этого отношения
     */
    private val scaleRelationships: MutableMap<String, List<String>> = HashMap()

    /**
     * Названия предикатов, задающих нумерацию для шкал
     *
     * key - имя отношения,
     * val - имя предиката нумерации
     */
    private val scalePredicates: MutableMap<String, String> = HashMap()

    /**
     * ID предиката
     */
    private var scalePredicateId = 0
        get() = ++field

    // ++++++++++++++++++++++++++++++++ Инициализация ++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * Инициализирует словарь данными
     * @param path Путь с фалу с данными для словаря
     */
    internal fun init(path: String) {
        // Очищаем старые значения
        relationships.clear()
        scaleRelationships.clear()
        scalePredicates.clear()

        // Создаем объекты
        val parser = CSVParserBuilder().withSeparator(COLUMNS_SEPARATOR).build()
        val bufferedReader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)
        val csvReader = CSVReaderBuilder(bufferedReader).withCSVParser(parser).build()

        // Считываем файл
        csvReader.use { reader ->
            val rows = reader.readAll()

            rows.forEach { row ->
                val name = row[0]
                val parent = row[1].ifBlank { null }
                val args = row[2]
                    .split(LIST_ITEMS_SEPARATOR)
                    .filter { it.isNotBlank() }
                    .ifEmpty { null }
                val scaleType = ScaleType.valueOf(row[3])
                val isRelation = row[4].toBoolean()
                val relationType = RelationType.valueOf(row[5])
                val scaleRelations = row[6]
                    .split(LIST_ITEMS_SEPARATOR)
                    .filter { it.isNotBlank() }
                    .ifEmpty { null }
                var flags = row[7].toInt()

                require(!exist(name)) {
                    "Отношение $name уже объявлено в словаре."
                }
                require(!isRelation || relationType != null) {
                    "Не указан тип связи между классами."
                }
                require(scaleType == null || scaleRelations != null && scaleRelations.size == 6) {
                    "Некорректное количество отношений порядковой шкалы для отношения $name."
                }
                require(args != null) {
                    "Не указаны аргументы для отношения $name."
                }

                when (scaleType) {
                    ScaleType.Linear -> {
                        require(flags == 6 || flags == 0) {
                            "Некорректный набор флагов для отношения линейного порядка."
                        }

                        scaleRelationships[name] = scaleRelations!!
                        flags = 6
                    }

                    ScaleType.Partial -> {
                        require(flags == 22 || flags == 0) {
                            "Некорректный набор флагов для отношения частичного порядка."
                        }
                        flags = 22
                    }

                    else -> {
                        require(flags < 64) {
                            "Некорректный набор флагов."
                        }
                    }
                }
                val r = RelationshipModel(
                    name = name,
                    parent = parent,
                    argsClasses = args,
                    scaleType = scaleType,
                    scaleRelationshipsNames = scaleRelations!!,
                    relationType = relationType,
                    flags = flags
                )

                relationships.add(r)
                relationships.addAll(r.scaleRelationships())
            }
        }
    }

    // ++++++++++++++++++++++++++++++++++++ Методы +++++++++++++++++++++++++++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    internal fun forEach(block: (RelationshipModel) -> Unit) {
        relationships.forEach(block)
    }

    /**
     * Получить предикат порядковой шкалы для отношения
     * @param name Имя отношения
     */
    internal fun getScalePredicate(name: String) = scalePredicates[name]

    /**
     * Получить модель отношения по имени
     * @param name Имя отношения
     */
    internal fun get(name: String) = relationships.firstOrNull { it.name == name }

    /**
     * Проверяет корректность содержимого словаря
     * @throws IllegalArgumentException
     */
    fun validate() {
        relationships.forEach {
            it.validate()
            require(it.scaleType == null || scalePredicates.containsKey(it.name))
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