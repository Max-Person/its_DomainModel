package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.NoneType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Условное выполнение вложенного оператора
 *
 * Ничего не возвращает ([NoneType])
 * @param conditionExpr условие, определяющее выполнение оператора [thenExpr] ([BooleanType])
 * @param thenExpr оператор-тело условия (тип игнорируется)
 */
class IfThen(
    val conditionExpr: Operator,
    val thenExpr: Operator,
) : Operator() {
    override val children: List<Operator>
        get() = listOf(conditionExpr, thenExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val conditionType = conditionExpr.validateAndGetType(domain, results, context)
        results.checkValid(
            conditionType is BooleanType,
            "Condition argument of a $description should be of boolean type, but was '$conditionType'"
        )

        thenExpr.validateAndGetType(domain, results, context)
        return NoneType
    }


    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}