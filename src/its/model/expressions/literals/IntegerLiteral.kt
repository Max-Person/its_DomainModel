package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Integer литерал
 * @param value Значение
 */
class IntegerLiteral(value: Int) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Integer

    override fun clone(): Operator = IntegerLiteral(value.toInt())

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}