package its.model

import its.model.Utils.plus
import its.model.definition.DomainModel
import its.model.definition.compat.DomainDictionariesRDFBuilder
import its.model.definition.loqi.DomainLoqiBuilder
import its.model.nodes.DecisionTree
import its.model.nodes.xml.DecisionTreeXMLBuilder
import java.io.File
import java.net.URL

/**
 * Решение задач в предметной области
 * @param domainModel описание предметной области (домена), общее для всех ситуаций применения
 * @param tagsData части предметной области, специфичные для отдельных ситуаций применения (т.н. теги)
 * @param decisionTrees деревья решений, описывающие решение задач в предметной области
 */
class DomainSolvingModel(
    val domainModel: DomainModel,
    val tagsData: Map<String, DomainModel>,
    val decisionTrees: Map<String, DecisionTree>,
) {

    enum class BuildMethod {
        LOQI,
        DICT_RDF,
    }

    /**
     * Построить модель на основе данных, взятых из директории [directoryUrl]
     *
     * - Ожидается, что словари являются файломи в данной директории и называются
     * `'enums.csv'`, `'classes.csv'`, `'properties.csv'` и `'relationships.csv'` соответственно.
     * - RDF-данные аналогично читаются из turtle-файла `'domain.ttl'`
     * - Деревья решений аналогично читаются из XML файлов вида tree_<имя дерева>.xml
     */
    constructor(directoryURL: URL, buildMethod: BuildMethod = BuildMethod.DICT_RDF) : this(
        collectDomain(directoryURL, buildMethod),
        collectTags(directoryURL),
        collectTrees(directoryURL)
    )

    constructor(directoryPath: String, buildMethod: BuildMethod = BuildMethod.DICT_RDF)
            : this(File(directoryPath).toURI().toURL(), buildMethod)


    constructor(domainModel: DomainModel, decisionTreeDirectoryURL: URL)
            : this(domainModel, emptyMap(), collectTrees(decisionTreeDirectoryURL))

    constructor(domainModel: DomainModel, decisionTreeDirectoryPath: String)
            : this(domainModel, File(decisionTreeDirectoryPath).toURI().toURL())


    companion object {
        /**
         * Построить домен на основе файлов в директории
         */
        @JvmStatic
        fun collectDomain(directoryURL: URL, buildMethod: BuildMethod = BuildMethod.DICT_RDF): DomainModel {
            return when (buildMethod) {
                BuildMethod.LOQI -> DomainLoqiBuilder.buildDomain(
                    (directoryURL + "domain.loqi").openStream().bufferedReader()
                )

                BuildMethod.DICT_RDF -> DomainDictionariesRDFBuilder.buildDomain(directoryURL)
            }
        }

        @JvmStatic
        fun collectTags(directoryURL: URL): Map<String, DomainModel> {
            return DirectoryScanUtils.findFilesMatching(directoryURL, Regex("tag_(\\S+)\\.loqi"))
                .map { (fileUrl, regexMatch) ->
                    val (name) = regexMatch.destructured
                    name to DomainLoqiBuilder.buildDomain(fileUrl.openStream().bufferedReader())
                }
                .toMap()
        }

        /**
         * Построить набор деревьев решений на основе файлов в директории
         */
        @JvmStatic
        fun collectTrees(directoryURL: URL): Map<String, DecisionTree> {
            return DirectoryScanUtils.findFilesMatching(directoryURL, Regex("tree(_\\S+|)\\.xml"))
                .map { (fileUrl, regexMatch) ->
                    var (name) = regexMatch.destructured
                    if (name.startsWith("_")) {
                        name = name.substring(1)
                    }
                    name to DecisionTreeXMLBuilder.fromXMLFile(fileUrl.toURI().toString())
                }
                .toMap()
        }
    }


    /**
     * Валидация модели с выкидыванием исключений
     * @return this
     */
    fun validate(): DomainSolvingModel {
        domainModel.validateAndThrow()
        tagsData.keys.forEach { tagName ->
            val mergedTagDomain = getMergedTagDomain(tagName)
            mergedTagDomain.validateAndThrow() //Деревья должны корректно работать со всеми теговыми моделями
            decisionTrees.values.forEach { it.validate(mergedTagDomain) }
        }
        return this
    }

    /**
     * Дерево решений "По умолчанию" - дерево решений без имени
     */
    val decisionTree: DecisionTree
        get() {
            require(decisionTrees.containsKey("")) { "DomainModel does not have a default decisionTree. Use decisionTree(name) instead." }
            return decisionTrees[""]!!
        }

    /**
     * Получить дерево решений по имени
     */
    fun decisionTree(name: String): DecisionTree {
        require(decisionTrees.containsKey(name)) { "DomainModel does not have a decisionTree named '$name'." }
        return decisionTrees[name]!!
    }

    /**
     * Получить обобщенную модель домена,
     * созданную из [domainModel]-модели и тег-модели из [tagsData] по переданному имени [name]
     */
    fun getMergedTagDomain(name: String): DomainModel {
        require(tagsData.containsKey(name)) { "DomainSolvingModel does not have a tag Domain model named '$name'" }
        return domainModel.copy().apply { addMerge(tagsData[name]!!) }
    }

}