package its.model.nodes

import its.model.TypedVariable
import its.model.definition.Domain
import its.model.definition.types.ObjectType
import its.model.expressions.Operator


/**
 * Присвоение переменной в дереве решений
 *
 * Создает или заменяет переменную дерева решений [variable] со значением (объектом), вычисляемым по [valueExpr]
 *
 * @param variable определяемая переменная дерева решений
 * @param valueExpr выражение, вычисляющее значение переменной ([ObjectType])
 */
class DecisionTreeVarAssignment(
    val variable: TypedVariable,
    val valueExpr: Operator,
) : HelperDecisionTreeElement() {
    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        super.validate(domain, results, context)
        variable.checkValid(domain, results, context, this)
        val valueType = valueExpr.validateForDecisionTree(domain, results, context)
        results.checkValid(
            valueType is ObjectType && ObjectType(variable.className).castFits(valueType, domain),
            "Value expression in $description ($parent) returns $valueType, but must return " +
                    "an object of type '${variable.className}' to conform to a variable ${variable.varName}"
        )
    }
}

internal fun List<DecisionTreeVarAssignment>.validate(
    domain: Domain,
    results: DecisionTreeValidationResults,
    context: DecisionTreeContext,
    owner: DecisionTreeElement
) {
    forEach { it.validate(domain, results, context) }
    for ((i, secondaryAssignment) in this.withIndex()) {
        val others = this.subList(i + 1, this.size)
        results.checkValid(
            others.none { it.variable.varName == secondaryAssignment.variable.varName },
            "Cannot declare multiple secondary assignments " +
                    "with the same name ${secondaryAssignment.variable.varName} (in $owner)"
        )
    }
    forEach { context.add(it.variable) }
}