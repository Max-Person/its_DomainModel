package its.model.expressions

import its.model.nodes.DecisionTreeContext

/**
 * Контекст проверки выражений
 * @param variableTypes соответствие имен контекстных переменных и названий их типов
 * @param decisionTreeVariableTypes соответствие имен переменных дерева решений и названий их типов
 */
class ExpressionContext(
    variableTypes: MutableMap<String, String> = mutableMapOf(),
    val decisionTreeVariableTypes: Map<String, String> = mapOf(),
) : DecisionTreeContext(variableTypes) {
    companion object {
        @JvmStatic
        fun from(decisionTreeContext: DecisionTreeContext) =
            ExpressionContext(decisionTreeVariableTypes = decisionTreeContext.variableTypes)
    }
}