package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.AnyType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Сохранение получаемого объекта в контекстную переменную для дальнейших вычислений
 *
 * Возвращает тип вложенного выражения [nestedExpr]
 * @param objExpr сохраняемый объект ([ObjectType])
 * @param varName имя контекстной переменной
 * @param nestedExpr вложенное выражение ([AnyType])
 */
class With(
    val objExpr: Operator,
    val varName: String,
    val nestedExpr: Operator,
) : Operator() {
    override val children: List<Operator>
        get() = listOf(objExpr, nestedExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val objType = objExpr.validateAndGetType(domain, results, context)
        if (objType !is ObjectType) {
            results.invalid(
                "Object-argument of $description should be an object, but was $objType"
            )
            return AnyType
        }

        context.variableTypes[varName] = objType.className
        val type = nestedExpr.validateAndGetType(domain, results, context)
        context.variableTypes.remove(varName)
        return type
    }


    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}