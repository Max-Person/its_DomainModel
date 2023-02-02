package its.model.nodes

import its.model.expressions.Operator
import its.model.models.DecisionTreeVarModel

interface DecisionTreeVarDeclaration {
    fun declaredVariable(): DecisionTreeVarModel
    fun declarationExpression() : Operator
}