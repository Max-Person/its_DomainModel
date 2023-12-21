package its.model.expressions.operators

import its.model.definition.Domain
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.literals.ClassLiteral
import its.model.expressions.literals.VariableLiteral

data class TypedVariable(
    val className: String,
    val varName: String,
) {
    fun checkValid(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext,
        declaringOperator: Operator,
    ): Boolean {
        var valid = true
        if (domain.classes.get(className).isEmpty) {
            results.nonConforming(
                "No class of name '$className' found in domain, " +
                        "but it was declared as a type for variable in ${declaringOperator.description}"
            )
            valid = false
        }

        if (context.variableTypes.containsKey(varName)) {
            results.invalid(
                "Variable $varName of type $className declared in ${declaringOperator.description} " +
                        "shadows variable $varName of type ${context.variableTypes[varName]} declared previously"
            )
            valid = false
        }

        return valid
    }
}

fun getTypeFromConditionExpr(condition: Operator, varName: String, opName: String): String {
    val types = getTypesFromConditionExpr(condition, varName)
    if (types.size != 1) {
        throw IllegalStateException("Cannot infer type for variable '$varName' in $opName operator")
    }
    return types.single()
}

fun getTypesFromConditionExpr(condition: Operator, varName: String): List<String> {
    if (condition is LogicalAnd) {
        return getTypesFromConditionExpr(condition.firstExpr, varName)
            .plus(getTypesFromConditionExpr(condition.secondExpr, varName))
    }
    if (condition is CheckClass
        && condition.objectExpr is VariableLiteral && condition.objectExpr.name == varName
        && condition.classExpr is ClassLiteral
    ) {
        return listOf(condition.classExpr.name)
    }
    if (condition is CompareWithComparisonOperator && condition.operator == CompareWithComparisonOperator.ComparisonOperator.Equal
        && condition.firstExpr is GetClass
        && condition.firstExpr.objectExpr is VariableLiteral && condition.firstExpr.objectExpr.name == varName
        && condition.secondExpr is ClassLiteral
    ) {
        return listOf(condition.secondExpr.name)
    }
    return listOf()
}