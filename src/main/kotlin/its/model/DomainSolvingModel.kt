package its.model

import its.model.Utils.plus
import its.model.definition.Domain
import its.model.definition.compat.DomainDictionariesRDFBuilder
import its.model.definition.loqi.DomainLoqiBuilder
import its.model.nodes.DecisionTree
import its.model.nodes.xml.DecisionTreeXMLBuilder
import java.io.File
import java.net.URL

/**
 * Решение задач в предметной области
 * @param domain описание предметной области (домена)
 * @param decisionTrees деревья решений, описывающие решение задач в предметной области
 */
class DomainSolvingModel(
    val domain: Domain,
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
    constructor(directoryURL: URL, buildMethod: BuildMethod = BuildMethod.DICT_RDF)
            : this(collectDomain(directoryURL, buildMethod), directoryURL)

    constructor(directoryPath: String, buildMethod: BuildMethod = BuildMethod.DICT_RDF)
            : this(File(directoryPath).toURI().toURL(), buildMethod)


    constructor(domain: Domain, decisionTreeDirectoryURL: URL)
            : this(domain, collectTrees(decisionTreeDirectoryURL))

    constructor(domain: Domain, decisionTreeDirectoryPath: String)
            : this(domain, File(decisionTreeDirectoryPath).toURI().toURL())


    companion object {
        /**
         * Построить домен на основе файлов в директории
         */
        @JvmStatic
        fun collectDomain(directoryURL: URL, buildMethod: BuildMethod = BuildMethod.DICT_RDF): Domain {
            return when (buildMethod) {
                BuildMethod.LOQI -> DomainLoqiBuilder.buildDomain(
                    (directoryURL + "domain.loqi").openStream().bufferedReader()
                )

                BuildMethod.DICT_RDF -> DomainDictionariesRDFBuilder.buildDomain(directoryURL)
            }
        }

        /**
         * Построить набор деревьев решений на основе файлов в директории
         */
        @JvmStatic
        fun collectTrees(directoryURL: URL): Map<String, DecisionTree> {
            return DirectoryScanUtils.findFilesMatching(directoryURL, Regex("tree(_\\w+|)\\.xml"))
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
        domain.validateAndThrow()
        decisionTrees.values.forEach { it.validate(domain) }
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

}