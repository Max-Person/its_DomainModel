package its.model.nodes

import its.model.definition.DomainModel
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел логической агрегации по ветвям
 *
 * Выполняет (условно параллельно) все ветви [thoughtBranches] и агрегирует их результаты по методу [aggregationMethod];
 * Дальнейшие переходы осуществляются в зависимости от результата агрегации
 *
 * @param aggregationMethod метод агрегации результатов ветвей мысли
 * @param thoughtBranches ветви мысли, результаты которых агрегируются в данном узле
 */
class BranchAggregationNode(
    override val aggregationMethod: AggregationMethod,
    val thoughtBranches: List<ThoughtBranch>,
    override val outcomes: Outcomes<BranchResult>,
) : AggregationNode() {

    override val linkedElements: List<DecisionTreeElement>
        get() = thoughtBranches.toList().plus(outcomes)

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        results.checkValid(
            thoughtBranches.isNotEmpty(),
            "$description has to have at least one ThoughtBranch"
        )

        validateLinked(domainModel, results, context)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}