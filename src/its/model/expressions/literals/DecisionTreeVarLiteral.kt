package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Литерал переменной из дерева рассуждения
 * @param value Имя переменной из дерева рассуждения
 */
class DecisionTreeVarLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.DecisionTreeVar

    override fun clone(): Operator = DecisionTreeVarLiteral(value)

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}