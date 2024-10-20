package its.model.nodes

import its.model.definition.DomainModel
import its.model.expressions.Operator
import its.model.nodes.visitors.DecisionTreeBehaviour

/**
 * Узел-результат ветви мысли [ThoughtBranch]
 * @param value возвращаемое узлом значение
 * @param actionExpr выполняемое при достижении данного узла побочное действие
 */
class BranchResultNode(
    val value: BranchResult,
    val actionExpr: Operator?,
) : DecisionTreeNode(), EndingNode {

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf()

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        actionExpr?.also { it.validateForDecisionTree(domainModel, results, context) }
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}