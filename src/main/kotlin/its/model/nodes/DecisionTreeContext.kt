package its.model.nodes

import its.model.definition.types.Type

/**
 * Контекст проверки деревьев решений
 * @param decisionTreeVariableTypes соответствие имен переменных дерева решений и названий их типов
 */
open class DecisionTreeContext(
    val decisionTreeVariableTypes: MutableMap<String, String> = mutableMapOf(),
)
