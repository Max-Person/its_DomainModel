package its.model.nodes

import its.model.ValueTuple
import its.model.definition.DomainModel
import its.model.definition.types.Type
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour
import mp.utils.SetOnce

/**
 * Узел вопроса-кортежа
 *
 * Вычисляет несколько независимых частей-вопросов [parts], и совершает переход по комбинации их результатов
 *
 * @param [parts] части кортежа, вычисляемые в узле
 */
class TupleQuestionNode(
    val parts: List<TupleQuestionPart>,
    override val outcomes: Outcomes<ValueTuple>,
) : LinkNode<ValueTuple>() {

    init {
        parts.forEach { it.owner = this }
    }

    override val linkedElements: List<DecisionTreeElement>
        get() = outcomes.toList()

    class TupleQuestionPart(
        val expr: Operator,
        val possibleOutcomes: List<TupleQuestionOutcome>
    ) : HelperDecisionTreeElement() {
        var owner: TupleQuestionNode by SetOnce()

        override val description: String
            get() = "${owner.description} (${super.description})"

        override fun validate(
            domainModel: DomainModel,
            results: DecisionTreeValidationResults,
            context: DecisionTreeContext
        ) {
            val exprType = expr.validateForDecisionTree(domainModel, results, context)
            results.checkValid(possibleOutcomes.size >= 2, "$description must contain at least two outcomes")
            for (outcome in possibleOutcomes) {
                val outcomeType = Type.of(outcome.value)
                results.checkValid(
                    exprType.castFits(outcomeType, domainModel),
                    "Outcome key '${outcome.value}' cannot be cast to the expression's type '$exprType' (in $description)"
                )
            }
        }
    }

    class TupleQuestionOutcome(
        val value: Any
    ) : HelperDecisionTreeElement()

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        results.checkValid(
            parts.size >= 2,
            "$description must contain at least two question parts (for a single part use a regular QuestionNode)"
        )
        validateLinked(domainModel, results, context, parts)
        val exprTypes = parts.map { it.expr.validateForDecisionTree(domainModel, results, context) }
        for (outcome in outcomes) {
            results.checkValid(
                outcome.key.size == parts.size,
                "In $description a ValueTuple outcome key '${outcome.key}' must contain as many values " +
                        "as there are parts in the node; ${parts.size} were expected, " +
                        "but ${outcome.key.size} were found"
            )
            exprTypes.forEachIndexed { i, exprType ->
                val outcomeValue = outcome.key[i] ?: return
                val outcomeType = Type.of(outcomeValue)
                results.checkValid(
                    exprType.castFits(outcomeType, domainModel),
                    "Outcome value '${outcomeValue}' cannot be cast to the type '$exprType' " +
                            "of a corresponding expression in $description"
                )
            }
        }
        validateLinked(domainModel, results, context)
    }


    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.processTupleQuestionNode(this)
    }
}