package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.AnyType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Последовательное выполнение нескольких операторов
 *
 * Возвращает последнее из вложенных выражений [nestedExprs] (типизируется соответствующе)
 * @param nestedExprs вложенные операторы ([AnyType])
 */
class Block(
    val nestedExprs: List<Operator>
) : Operator() {
    override val children: List<Operator>
        get() = nestedExprs

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        return nestedExprs.map { it.validateAndGetType(domainModel, results, context) }.last()
    }


    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}