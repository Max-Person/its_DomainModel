package its.model.nodes

import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.Type
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел вопроса
 *
 * Вычисляет выражение [expr] и совершает переход, чей ключ соответствует вычисленному результату
 *
 * @param [expr] выражение, вычисляемое в узле
 * @param [isSwitch] является ли узел 'switch'-узлом. 'switch'-узлы считаются тривиальными (см. [trivialityExpr])
 * @param [trivialityExpr] условие тривиальности узла ([BooleanType]).
 * Если узел тривиален, на нем не должно заостряться внимание студента
 */
class QuestionNode(
    val expr: Operator,
    override val outcomes: Outcomes<Any>,
    val isSwitch: Boolean = false,
    val trivialityExpr: Operator? = null,
) : LinkNode<Any>() {
    override val linkedElements: List<DecisionTreeElement>
        get() = outcomes.values.toList()

    val canBeTrivial: Boolean
        get() = isSwitch || trivialityExpr != null

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        val exprType = expr.validateForDecisionTree(domain, results, context)
        for (outcome in outcomes.values) {
            val outcomeType = Type.of(outcome.key)
            results.checkValid(
                exprType.castFits(outcomeType, domain),
                "Outcome key '${outcome.key}' cannot be cast to the node's type '$exprType' (in $description)"
            )
        }
        if (trivialityExpr != null) {
            val trivialityType = trivialityExpr.validateForDecisionTree(domain, results, context)
            results.checkValid(
                trivialityType is BooleanType,
                "Triviality expression for the $description returns $trivialityType, but must return a boolean"
            )
        }
        validateLinked(domain, results, context)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}