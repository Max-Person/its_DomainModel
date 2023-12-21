package its.model.expressions

import its.model.definition.types.Type
import its.model.nodes.DecisionTreeContext

/**
 * Контекст проверки выражений
 * @param variableTypes соответствие имен контекстных переменных и названий их типов
 * @param decisionTreeVariableTypes соответствие имен переменных дерева решений и названий их типов
 */
class ExpressionContext(
    val variableTypes: MutableMap<String, String> = mutableMapOf(),
    decisionTreeVariableTypes: MutableMap<String, String> = mutableMapOf(),
) : DecisionTreeContext(decisionTreeVariableTypes)