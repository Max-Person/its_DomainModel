package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.NoneType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Последовательное выполнение нескольких операторов
 *
 * Ничего не возвращает ([NoneType])
 * @param nestedExprs вложенные операторы (тип игнорируется)
 */
class Block(
    val nestedExprs: List<Operator>
) : Operator() {
    override val children: List<Operator>
        get() = nestedExprs

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        nestedExprs.forEach { it.validateAndGetType(domain, results, context) }
        return NoneType
    }


    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}