package its.model.nodes

import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел агрегации в цикле 'while'
 *
 * Выполняет ветку [thoughtBranch], пока выполняется условие [conditionExpr]
 * и агрегирует результаты каждого выполнения ветви по оператору [logicalOp];
 * Дальнейшие переходы осуществляются в зависимости от результата агрегации
 *
 * @param logicalOp логический оператор, агрегирующий результаты каждой итерации цикла
 * @param conditionExpr условие продолжения выполнения цикла
 * @param thoughtBranch ветвь мысли, представляющая тело цикла
 */
class WhileAggregationNode(
    override val logicalOp: LogicalOp,
    val conditionExpr: Operator,
    val thoughtBranch: ThoughtBranch,
    override val outcomes: Outcomes<Boolean>,
) : AggregationNode() {

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
            outcomes.containsKey(true) && outcomes.containsKey(false),
            "$description has to have both true and false outcomes"
        )

        validateLinked(domainModel, results, context)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}