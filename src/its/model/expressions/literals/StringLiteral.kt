package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.LiteralBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * String литерал
 * @param value Значение
 */
class StringLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.String

    override fun clone(): Operator = StringLiteral(value)

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this)
    }

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}