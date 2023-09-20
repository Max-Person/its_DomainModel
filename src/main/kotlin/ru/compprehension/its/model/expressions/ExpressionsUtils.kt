package ru.compprehension.its.model.expressions

import ru.compprehension.its.model.expressions.literals.DecisionTreeVar
import ru.compprehension.its.model.expressions.operators.BaseOperator

fun Operator.getUsedVariables(): Set<String> {
    val set = mutableSetOf<String>()
    if (this is DecisionTreeVar) {
        set.add(this.name)
    } else if (this is BaseOperator) {
        this.args.forEach { set.addAll(it.getUsedVariables()) }
    }
    return set
}