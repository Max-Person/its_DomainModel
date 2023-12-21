package its.model.expressions

import its.model.expressions.literals.DecisionTreeVarLiteral

fun Operator.getUsedVariables(): Set<String> {
    val set = mutableSetOf<String>()
    if (this is DecisionTreeVarLiteral) {
        set.add(this.name)
    } else if (this is Operator) {
        this.children.forEach { set.addAll(it.getUsedVariables()) }
    }
    return set
}