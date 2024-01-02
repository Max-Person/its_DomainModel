package its.model.nodes

import its.model.TypedVariable

/**
 * Контекст проверки деревьев решений
 * @param variableTypes соответствие имен переменных дерева решений и названий их типов
 */
open class DecisionTreeContext(
    open val variableTypes: MutableMap<String, String> = mutableMapOf(),
) {
    fun add(variable: TypedVariable) {
        variableTypes[variable.varName] = variable.className
    }

    fun remove(variable: TypedVariable) {
        variableTypes.remove(variable.varName)
    }
}
