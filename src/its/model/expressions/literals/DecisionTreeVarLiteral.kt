package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.DECISION_TREE_VAR_PREDICATE
import its.model.util.JenaUtil.POAS_PREF
import its.model.util.JenaUtil.genTriple
import its.model.util.NamingManager

/**
 * Литерал переменной из дерева рассуждения
 * @param value Имя переменной из дерева рассуждения
 */
class DecisionTreeVarLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.DecisionTreeVar

    override fun compile(): CompilationResult {
        val resVarName = NamingManager.genVarName()
        return CompilationResult(
            value = resVarName,
            heads = listOf(
                genTriple(
                    resVarName,
                    JenaUtil.genLink(POAS_PREF, DECISION_TREE_VAR_PREDICATE),
                    JenaUtil.genVal(value)
                )
            )
        )
    }

    override fun clone(): Operator = DecisionTreeVarLiteral(value)
}