package its.model.nodes

import its.model.definition.Domain
import its.model.expressions.Operator
import its.model.nodes.visitors.DecisionTreeBehaviour
import java.util.*

/**
 * Узел-результат ветви мысли [ThoughtBranch]
 * @param value возвращаемое узлом значение
 * @param actionExpr выполняемое при достижении данного узла побочное действие
 */
class BranchResultNode(
    val value: Boolean, //TODO исправить когда перейдем к зеленым деревьям
    val actionExpr: Optional<Operator>,
) : DecisionTreeNode() {

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf()

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        actionExpr.ifPresent { it.validateForDecisionTree(domain, results, context) }
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}