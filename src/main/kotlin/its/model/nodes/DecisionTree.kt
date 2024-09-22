package its.model.nodes

import its.model.TypedVariable
import its.model.definition.DomainModel

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

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        variables.forEach {
            it.checkValid(domainModel, results, context, this)
            context.add(it)
        }
        implicitVariables.validate(domainModel, results, context, this)
        mainBranch.validate(domainModel, results, context)
    }
}