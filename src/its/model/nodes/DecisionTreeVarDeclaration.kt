package its.model.nodes

interface DecisionTreeVarDeclaration {
    fun declaredVariables(): Map<String, String>
}