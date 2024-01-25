package its.model.nodes

import its.model.definition.Domain
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел логической агрегации по ветвям
 *
 * Выполняет (условно параллельно) все ветви [thoughtBranches] и агрегирует их результаты по оператору [logicalOp];
 * Дальнейшие переходы осуществляются в зависимости от результата агрегации
 *
 * @param logicalOp логический оператор, агрегирующий результаты каждой итерации цикла
 * @param thoughtBranches ветви мысли, результаты которых агрегируются в данном узле
 */
class LogicAggregationNode(
    val logicalOp: LogicalOp,
    val thoughtBranches: List<ThoughtBranch>,
    override val outcomes: Outcomes<Boolean>,
) : LinkNode<Boolean>() {

    override val linkedElements: List<DecisionTreeElement>
        get() = thoughtBranches.toList().plus(outcomes)

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        results.checkValid(
            outcomes.containsKey(true) && outcomes.containsKey(false),
            "$description has to have both true and false outcomes"
        )
        results.checkValid(
            thoughtBranches.isNotEmpty(),
            "$description has to have at least one ThoughtBranch"
        )

        validateLinked(domain, results, context)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}