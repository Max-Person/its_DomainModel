package its.model.nodes

import its.model.TypedVariable
import its.model.definition.Domain

/**
 * Дерево решений (рассуждений/мысли), описывающее решение задач внутри домена
 */
class DecisionTree(
    val variables: List<TypedVariable>,
    val implicitVariables: List<DecisionTreeVarAssignment>,
    val mainBranch: ThoughtBranch,
) : DecisionTreeElement() {
    init {
        decisionTree = this
        parent = this
        setupLinks(this)
    }

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(mainBranch).plus(implicitVariables)

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        variables.forEach {
            it.checkValid(domain, results, context, this)
            context.add(it)
        }
        implicitVariables.validate(domain, results, context, this)
        mainBranch.validate(domain, results, context)
    }
}