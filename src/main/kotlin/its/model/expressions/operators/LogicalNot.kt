package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Логическое отрицание
 *
 * Возвращает [BooleanType]
 * @param operandExpr аргумент выражения ([BooleanType])
 */
class LogicalNot(
    val operandExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(operandExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val opType = operandExpr.validateAndGetType(domain, results, context)
        results.checkValid(
            opType is BooleanType,
            "Argument of a $description should be of boolean type, but was '$opType'"
        )
        return BooleanType
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}