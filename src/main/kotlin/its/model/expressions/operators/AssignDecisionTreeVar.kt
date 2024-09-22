package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.NoneType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Присвоить переменной дерева решений значение (объект)
 *
 * Ничего не возвращает ([NoneType])
 * @param variableName имя переменной
 * @param valueExpr значение-объект ([ObjectType])
 */
class AssignDecisionTreeVar(
    val variableName: String,
    val valueExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(valueExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = NoneType

        val valueType = valueExpr.validateAndGetType(domainModel, results, context)

        if (!context.decisionTreeVariableTypes.containsKey(variableName)) {
            results.invalid("No decision tree variable '$variableName' is known to assign in $description")
            return type
        }
        val variableType = ObjectType(context.decisionTreeVariableTypes[variableName]!!)

        results.checkValid(
            variableType.castFits(valueType, domainModel),
            "Cannot assign a value of type $valueType to a decision tree variable $variableName of type $variableType"
        )
        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}