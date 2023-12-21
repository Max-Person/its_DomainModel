package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.ComparisonType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Квантор существования (Возвращает [BooleanType])
 *
 * Возвращает [BooleanType]
 * @param variable контекстная переменная, задающая ссылку на проверяемый объект
 * @param selectorExpr условие, задающее область определения переменной ([BooleanType])
 * @param conditionExpr условие, предъявляемое к переменной в области определения ([BooleanType])
 */
class ExistenceQuantifier(
    val variable: TypedVariable,
    val selectorExpr: Operator,
    val conditionExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(selectorExpr, conditionExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        variable.checkValid(domain, results, context, this)

        context.variableTypes[variable.varName] = variable.className
        val selectorType = selectorExpr.validateAndGetType(domain, results, context)
        results.checkValid(
            selectorType is BooleanType,
            "Selector argument of $description should be of boolean type, but was '$selectorType'"
        )
        val conditionType = conditionExpr.validateAndGetType(domain, results, context)
        results.checkValid(
            conditionType is BooleanType,
            "Condition argument of $description should be of boolean type, but was '$conditionType'"
        )
        context.variableTypes.remove(variable.varName)

        return BooleanType
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}