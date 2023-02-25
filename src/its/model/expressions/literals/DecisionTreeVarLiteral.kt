package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Литерал переменной из дерева рассуждения
 * @param value Имя переменной из дерева рассуждения
 */
class DecisionTreeVarLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.DecisionTreeVar

    override fun clone(): Operator = DecisionTreeVarLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}