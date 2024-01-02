package its.model.expressions.operators

import its.model.TypedVariable
import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить объект по условию
 *
 * Возвращает [ObjectType], выбрасывает ошибку если подходящих объектов нет, или их несколько
 * @param variable контекстная переменная, задающая ссылку на проверяемый объект
 * @param conditionExpr условие, предъявляемое к объекту ([BooleanType])
 */
class GetByCondition(
    val variable: TypedVariable,
    val conditionExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(conditionExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        variable.checkValid(domain, results, context, this)

        context.variableTypes[variable.varName] = variable.className
        val conditionType = conditionExpr.validateAndGetType(domain, results, context)
        context.variableTypes.remove(variable.varName)
        results.checkValid(
            conditionType is BooleanType,
            "Condition argument of $description should be of boolean type, but was '$conditionType'"
        )

        return ObjectType(variable.className)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}