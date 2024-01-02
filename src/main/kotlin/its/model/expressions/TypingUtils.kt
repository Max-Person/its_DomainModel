package its.model.expressions

import its.model.expressions.literals.ClassLiteral
import its.model.expressions.literals.VariableLiteral
import its.model.expressions.operators.CheckClass
import its.model.expressions.operators.CompareWithComparisonOperator
import its.model.expressions.operators.GetClass
import its.model.expressions.operators.LogicalAnd

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