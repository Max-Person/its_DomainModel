package its.model.expressions

import its.model.expressions.literals.DecisionTreeVar
import its.model.expressions.operators.BaseOperator

fun Operator.getUsedVariables() : Set<String>{
    val set = mutableSetOf<String>()
    if(this is DecisionTreeVar){
        set.add(this.name)
    }
    else if(this is BaseOperator){
        this.args.forEach{set.addAll(it.getUsedVariables())}
    }
    return set
}