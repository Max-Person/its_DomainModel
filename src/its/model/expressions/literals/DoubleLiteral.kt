package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Double литерал
 * @param value Значение
 */
class DoubleLiteral(value: Double) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Double

    override fun clone(): Operator = DoubleLiteral(value.toDouble())

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}