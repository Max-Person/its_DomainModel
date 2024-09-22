package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Логическое И
 *
 * Возвращает [BooleanType]
 * @param firstExpr первый аргумент ([BooleanType])
 * @param secondExpr первый аргумент  ([BooleanType])
 */
class LogicalAnd(
    val firstExpr: Operator,
    val secondExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(firstExpr, secondExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val firstType = firstExpr.validateAndGetType(domainModel, results, context)
        val secondType = secondExpr.validateAndGetType(domainModel, results, context)
        results.checkValid(
            firstType is BooleanType && secondType is BooleanType,
            "Both arguments of a $description should be of boolean type, but were '$firstType && $secondType'"
        )
        return BooleanType
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}