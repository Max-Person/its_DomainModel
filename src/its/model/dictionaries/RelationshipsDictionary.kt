package its.model.dictionaries

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.IS_BETWEEN_PATTERN
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.IS_BETWEEN_VAR_COUNT
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.IS_CLOSER_TO_THAN_PATTERN
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.IS_CLOSER_TO_THAN_VAR_COUNT
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.IS_FURTHER_FROM_THAN_PATTERN
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.IS_FURTHER_FROM_THAN_VAR_COUNT
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.REVERSE_PATTERN
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.REVERSE_TRANSITIVE_CLOSURE_PATTERN
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.REVERSE_TRANSITIVE_CLOSURE_VAR_COUNT
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.REVERSE_VAR_COUNT
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.TRANSITIVE_CLOSURE_PATTERN
import its.model.dictionaries.RelationshipsDictionary.LinerScalePatterns.TRANSITIVE_CLOSURE_VAR_COUNT
import its.model.dictionaries.util.DictionariesUtil.COLUMNS_SEPARATOR
import its.model.dictionaries.util.DictionariesUtil.LIST_ITEMS_SEPARATOR
import its.model.models.RelationshipModel
import its.model.models.RelationshipModel.Companion.RelationType
import its.model.models.RelationshipModel.Companion.ScaleType
import its.model.util.JenaUtil.POAS_PREF
import its.model.util.JenaUtil.genLink
import its.model.util.NamingManager.genPredicateName
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Словарь свойств
 */
object RelationshipsDictionary {

    // ++++++ Шаблоны вспомогательных правил для отношений порядковых шкал +++++++++
    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    internal object LinerScalePatterns {

        val NUMERATION_RULES_PATTERN = """
            
            [
            (?var1 <linerPredicate> ?var2)
            noValue(?var3, <linerPredicate>, ?var1)
            ->
            (?var1 <numberPredicate> "1"^^xsd:integer)
            ]
        
            [
            (?var1 <linerPredicate> ?var2)
            noValue(?var2, <numberPredicate>)
            (?var1 <numberPredicate> ?var3)
            addOne(?var3, ?var4)
            ->
            (?var2 <numberPredicate> ?var4)
            ]
            
        """.trimIndent()

        const val REVERSE_VAR_COUNT = 0
        val REVERSE_PATTERN = """
            (<arg2> <predicate> <arg1>)
        """.trimIndent()

        const val TRANSITIVE_CLOSURE_VAR_COUNT = 2
        val TRANSITIVE_CLOSURE_PATTERN = """
            (<arg1> <numberPredicate> <var1>)
            (<arg2> <numberPredicate> <var2>)
            lessThan(<var1>, <var2>)
        """.trimIndent()

        const val REVERSE_TRANSITIVE_CLOSURE_VAR_COUNT = 2
        val REVERSE_TRANSITIVE_CLOSURE_PATTERN = """
            (<arg1> <numberPredicate> <var1>)
            (<arg2> <numberPredicate> <var2>)
            greaterThan(<var1>, <var2>)
        """.trimIndent()

        const val IS_BETWEEN_VAR_COUNT = 3
        val IS_BETWEEN_PATTERN = """
            (<arg1> <numberPredicate> <var1>)
            (<arg2> <numberPredicate> <var2>)
            (<arg3> <numberPredicate> <var3>)
            greaterThan(<var1>, <var2>)
            lessThan(<var1>, <var3>)
        """.trimIndent()

        const val IS_CLOSER_TO_THAN_VAR_COUNT = 7
        val IS_CLOSER_TO_THAN_PATTERN = """
            (<arg1> <numberPredicate> <var1>)
            (<arg2> <numberPredicate> <var2>)
            (<arg3> <numberPredicate> <var3>)
            difference(<var2>, <var1>, <var4>)
            difference(<var2>, <var3>, <var5>)
            absoluteValue(<var4>, <var6>)
            absoluteValue(<var5>, <var7>)
            lessThan(<var6>, <var7>)
        """.trimIndent()

        const val IS_FURTHER_FROM_THAN_VAR_COUNT = 7
        val IS_FURTHER_FROM_THAN_PATTERN = """
            (<arg1> <numberPredicate> <var1>)
            (<arg2> <numberPredicate> <var2>)
            (<arg3> <numberPredicate> <var3>)
            difference(<var2>, <var1>, <var4>)
            difference(<var2>, <var3>, <var5>)
            absoluteValue(<var4>, <var6>)
            absoluteValue(<var5>, <var7>)
            greaterThan(<var6>, <var7>)
        """.trimIndent()
    }

    object PartialScalePatterns {
        // TODO
    }

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
                    ScaleType.Liner -> {
                        require(flags == 6 || flags == 0) {
                            "Некорректный набор флагов для отношения линейного порядка."
                        }

                        val scalePredicate = genPredicateName()
                        scaleRelationships[name] = scaleRelations!!
                        scalePredicates[name] = scalePredicate
                        flags = 6

                        relationships.addAll(
                            listOf(
                                RelationshipModel(
                                    name = scaleRelations[0],
                                    argsClasses = args,
                                    flags = flags,
                                    varsCount = REVERSE_VAR_COUNT,
                                    head = REVERSE_PATTERN.replace("<predicate>", genLink(POAS_PREF, name))
                                ),
                                RelationshipModel(
                                    name = scaleRelations[1],
                                    argsClasses = args,
                                    flags = 16,
                                    varsCount = TRANSITIVE_CLOSURE_VAR_COUNT,
                                    head = TRANSITIVE_CLOSURE_PATTERN.replace(
                                        "<numberPredicate>",
                                        genLink(POAS_PREF, scalePredicate)
                                    )
                                ),
                                RelationshipModel(
                                    name = scaleRelations[2],
                                    argsClasses = args,
                                    flags = 16,
                                    varsCount = REVERSE_TRANSITIVE_CLOSURE_VAR_COUNT,
                                    head = REVERSE_TRANSITIVE_CLOSURE_PATTERN.replace(
                                        "<numberPredicate>",
                                        genLink(POAS_PREF, scalePredicate)
                                    )
                                ),
                                RelationshipModel(
                                    name = scaleRelations[3],
                                    argsClasses = args.plus(args[0]),
                                    flags = 0,
                                    varsCount = IS_BETWEEN_VAR_COUNT,
                                    head = IS_BETWEEN_PATTERN.replace(
                                        "<numberPredicate>",
                                        genLink(POAS_PREF, scalePredicate)
                                    )
                                ),
                                RelationshipModel(
                                    name = scaleRelations[4],
                                    argsClasses = args.plus(args[0]),
                                    flags = 0,
                                    varsCount = IS_CLOSER_TO_THAN_VAR_COUNT,
                                    head = IS_CLOSER_TO_THAN_PATTERN.replace(
                                        "<numberPredicate>",
                                        genLink(POAS_PREF, scalePredicate)
                                    )
                                ),
                                RelationshipModel(
                                    name = scaleRelations[5],
                                    argsClasses = args.plus(args[0]),
                                    flags = 0,
                                    varsCount = IS_FURTHER_FROM_THAN_VAR_COUNT,
                                    head = IS_FURTHER_FROM_THAN_PATTERN.replace(
                                        "<numberPredicate>",
                                        genLink(POAS_PREF, scalePredicate)
                                    )
                                )
                            )
                        )
                    }

                    ScaleType.Partial -> {
                        require(flags == 22 || flags == 0) {
                            "Некорректный набор флагов для отношения частичного порядка."
                        }

                        val scalePredicate = genPredicateName()
                        scaleRelationships[name] = scaleRelations!!
                        scalePredicates[name] = scalePredicate
                        flags = 22

                        TODO("Отношения частичного порядка")
                    }

                    else -> {
                        require(flags < 64) {
                            "Некорректный набор флагов."
                        }
                    }
                }

                val varsCount = if (args.size == 2) {
                    0
                } else {
                    1
                }

                val head = if (args.size == 2) {
                    "(<arg1> ${genLink(POAS_PREF, name)} <arg2>)\n"
                } else {
                    var tmp = "(<arg1> ${genLink(POAS_PREF, name)} <var1>)\n"

                    args.forEachIndexed { index, _ ->
                        if (index != 0) {
                            tmp += "(<var1> ${genLink(POAS_PREF, name)} <arg${index + 1}>)\n"
                        }
                    }

                    tmp
                }

                relationships.add(
                    RelationshipModel(
                        name = name,
                        parent = parent,
                        argsClasses = args,
                        scaleType = scaleType,
                        relationType = relationType,
                        flags = flags,
                        varsCount = varsCount,
                        head = head
                    )
                )
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

    /**
     * Получить количество переменных в шаблоне правила
     * @param name Имя отношения
     */
    fun varsCount(name: String) = get(name)?.varsCount

    /**
     * Получить голову правила для проверки отношения
     * @param name Имя отношения
     */
    fun head(name: String) = get(name)?.head

    /**
     * Получить завершенные правила для проверки отношения
     * @param name Имя отношения
     */
    fun rules(name: String) = get(name)?.rules
}