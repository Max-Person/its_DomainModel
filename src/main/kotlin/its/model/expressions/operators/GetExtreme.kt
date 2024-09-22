package its.model.expressions.operators

import its.model.TypedVariable
import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить объект по условию экстремума
 *
 * Возвращает [ObjectType], выбрасывает ошибку если подходящих объектов нет, или их несколько
 * @param className тип контекстных переменных [varName] и [extremeVarName]
 * @param varName имя контекстной переменная, задающей ссылку на проверяемый объект в условии выборки [conditionExpr]
 * @param conditionExpr условие выборки, предъявляемое к объекту ([BooleanType])
 * @param extremeVarName имя контекстной переменной, задающая ссылку на проверяемый объект в условии экстремума [extremeConditionExpr]
 * @param extremeConditionExpr условие экстремума, предъявляемое к объекту ([BooleanType])
 */
class GetExtreme(
    val className: String,
    val varName: String,
    val conditionExpr: Operator,
    val extremeVarName: String,
    val extremeConditionExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(conditionExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        TypedVariable(className, varName).checkValid(domainModel, results, context, this)
        context.variableTypes[varName] = className
        val conditionType = conditionExpr.validateAndGetType(domainModel, results, context)
        context.variableTypes.remove(varName)
        results.checkValid(
            conditionType is BooleanType,
            "Condition argument of $description should be of boolean type, but was '$conditionType'"
        )

        TypedVariable(className, extremeVarName).checkValid(domainModel, results, context, this)
        context.variableTypes[extremeVarName] = className
        val extremeConditionType = extremeConditionExpr.validateAndGetType(domainModel, results, context)
        context.variableTypes.remove(extremeVarName)
        results.checkValid(
            extremeConditionType is BooleanType,
            "Extreme condition argument of $description should be of boolean type, but was '$extremeConditionType'"
        )

        return ObjectType(className)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}