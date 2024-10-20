package its.model.nodes

import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел последовательного перебора ветвей 'while'
 *
 * Выполняет ветку [thoughtBranch], пока выполняется условие [conditionExpr].
 * В случае, если очередная итерация цикла завершилась результатом, не равным [BranchResult.NULL],
 * то выполнение останавливается и данный результат возвращается.
 *
 * По смыслу похож на [CycleAggregationNode], но т.к. перебор типа "последовательность" имеет доп. ограничения
 * (порядок цикла), то вынесен в отдельный узел.
 *
 * @param conditionExpr условие продолжения выполнения цикла
 * @param thoughtBranch ветвь мысли, представляющая тело цикла
 */
class WhileCycleNode(
    val conditionExpr: Operator,
    val thoughtBranch: ThoughtBranch,
    override val outcomes: Outcomes<BranchResult>,
) : LinkNode<BranchResult>(), EndingNode {

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(thoughtBranch).plus(outcomes)

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        val conditionType = conditionExpr.validateForDecisionTree(domainModel, results, context)
        results.checkValid(
            conditionType is BooleanType,
            "Condition expression for the $description returns $conditionType, but must return a boolean"
        )
        results.checkValid(
            outcomes.containsKey(BranchResult.NULL),
            "$description has to have a NULL continuation outcome."
        )

        validateLinked(domainModel, results, context)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}