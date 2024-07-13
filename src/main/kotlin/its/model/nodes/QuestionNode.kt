package its.model.nodes

import its.model.ValueTuple
import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.Type
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел вопроса
 *
 * Вычисляет выражения [expressions] и совершает переход, чей ключ соответствует вычисленному результату
 * Если выражений несколько, то переход осуществляется по кортежу, составленному из их результатов
 *
 * @param [expressions] выражения, вычисляемое в узле
 * @param [isSwitch] является ли узел 'switch'-узлом. 'switch'-узлы считаются тривиальными (см. [trivialityExpr])
 * @param [trivialityExpr] условие тривиальности узла ([BooleanType]).
 * Если узел тривиален, на нем не должно заостряться внимание студента
 */
class QuestionNode(
    val expressions: List<Operator>,
    override val outcomes: Outcomes<Any>,
    val isSwitch: Boolean = false,
    val trivialityExpr: Operator? = null,
) : LinkNode<Any>() {

    val isTuple: Boolean
        get() = expressions.size > 1

    val expr: Operator
        get() = expressions.first()

    override val linkedElements: List<DecisionTreeElement>
        get() = outcomes.toList()

    val canBeTrivial: Boolean
        get() = isSwitch || trivialityExpr != null

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        results.checkValid(expressions.isNotEmpty(), "$description must contain at least one expression")
        val exprTypes = expressions.map { it.validateForDecisionTree(domain, results, context) }
        for (outcome in outcomes) {
            if (isTuple) {
                results.checkValid(
                    outcome.key is ValueTuple,
                    "Outcome key for a node with multiple expressions should be a ValueTuple, " +
                            "was '${outcome.key}' (in $description)"
                )
                outcome.key as ValueTuple
                results.checkValid(
                    outcome.key.size == expressions.size,
                    "A ValueTuple outcome key '${outcome.key}' must contain as many values " +
                            "as there are expressions in the node; ${expressions.size} were expected, " +
                            "but ${outcome.key.size} were found (in $description)"
                )
            }
            exprTypes.forEachIndexed { i, exprType ->
                val outcomeValue = if (!isTuple) outcome.key else ((outcome.key as ValueTuple)[i] ?: return)
                val outcomeType = Type.of(outcomeValue)
                results.checkValid(
                    exprType.castFits(outcomeType, domain),
                    "Outcome value '${outcomeValue}' cannot be cast to the type '$exprType' " +
                            "of a corresponding expression in $description"
                )
            }
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