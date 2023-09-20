package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.models.DecisionTreeVarModel

interface DecisionTreeVarDeclaration {
    fun declaredVariable(): DecisionTreeVarModel
    fun declarationExpression(): Operator
}