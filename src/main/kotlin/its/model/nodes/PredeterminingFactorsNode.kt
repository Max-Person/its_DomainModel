package its.model.nodes

import its.model.definition.Domain
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел предрешающих факторов ("Независимое ветвление")
 *
 * К переходам из данного узла привязаны ветви решения [ThoughtBranch],
 * результат выполнения которых определяют совершаемый из узла переход -
 * гарантируется, что в любой ситуации только одна из данных ветвей может дать положительный (`true`) результат.
 * Соответственно будет выполнен переход, соответствующий данной ветви.
 * В случае, если ни одна из ветвей не дала положительный результат,
 * будет выполнен 'неопределенный' переход - переход, к которому не привязана ветвь мысли
 */
class PredeterminingFactorsNode(
    override val outcomes: Outcomes<ThoughtBranch?>
) : LinkNode<ThoughtBranch?>() {
    override val linkedElements: List<DecisionTreeElement>
        get() = outcomes.toList()

    val predetermining
        get() = outcomes.filter { it.key != null }
    val undetermined
        get() = outcomes.singleOrNull { it.key == null }

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        results.checkValid(
            outcomes.filter { it.key == null }.size <= 1,
            "$description cannot have more than one undetermined outcome (outcome without an associated ThoughtBranch)"
        )
        validateLinked(domain, results, context)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}