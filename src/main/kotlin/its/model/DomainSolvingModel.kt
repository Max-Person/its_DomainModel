package its.model

import its.model.Utils.plus
import its.model.definition.Domain
import its.model.definition.compat.DomainDictionariesRDFBuilder
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

    /**
     * Построить модель на основе данных, взятых из директории [directoryUrl]
     *
     * - Ожидается, что словари являются файломи в данной директории и называются
     * `'enums.csv'`, `'classes.csv'`, `'properties.csv'` и `'relationships.csv'` соответственно.
     * - RDF-данные аналогично читаются из turtle-файла `'domain.ttl'`
     * - Деревья решений аналогично читаются из XML файлов вида tree_<имя дерева>.xml
     */
    constructor(directoryURL: URL) : this(
        DomainDictionariesRDFBuilder.buildDomain(directoryURL),
        directoryURL.let {
            val treeRegex = Regex("tree(_\\w+|)\\.xml")
            val trees = mutableMapOf<String, DecisionTree>()
            directoryURL.openStream().bufferedReader().lines().forEach {
                if (treeRegex.matches(it)) {
                    var (name) = treeRegex.find(it)!!.destructured
                    if (name.startsWith("_")) name = name.substring(1)
                    trees[name] = DecisionTreeXMLBuilder.fromXMLFile((directoryURL + it).toURI().toString())
                }
            }
            trees
        },
    )

    constructor(directoryPath: String) : this(File(directoryPath).toURI().toURL())

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