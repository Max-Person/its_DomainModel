package its.model.nodes

import its.model.TypedVariable
import its.model.definition.Domain

/**
 * Дерево решений (рассуждений/мысли), описывающее решение задач внутри домена
 */
class DecisionTree(
    val variables: List<TypedVariable>,
    val mainBranch: ThoughtBranch,
) : DecisionTreeElement() {
    init {
        decisionTree = this
        parent = this
        setupLinks(this)
    }

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(mainBranch)

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        variables.forEach { context.add(it) }
        validateLinked(domain, results, context)
    }
}