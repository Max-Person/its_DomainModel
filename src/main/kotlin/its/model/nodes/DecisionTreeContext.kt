package its.model.nodes

import its.model.TypedVariable
import its.model.definition.types.ObjectType
import its.model.definition.types.Type

/**
 * Контекст проверки деревьев решений
 * @param variableTypes соответствие имен переменных дерева решений и названий их типов
 */
open class DecisionTreeContext(
    open val variableTypes: MutableMap<String, Type<*>> = mutableMapOf(),
) {
    /**
     * Добавить объектную ([ObjectType]) переменную
     */
    fun add(variable: TypedVariable) {
        variableTypes[variable.varName] = ObjectType(variable.className)
    }

    /**
     * Удалить переменную из контекста
     */
    fun remove(variable: TypedVariable) {
        variableTypes.remove(variable.varName)
    }
}
