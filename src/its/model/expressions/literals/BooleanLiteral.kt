package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.LiteralBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Boolean литерал
 * @param value Значение
 */
class BooleanLiteral(value: Boolean) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Boolean


    override fun clone(): Operator = BooleanLiteral(value.toBoolean())

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this)
    }

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}