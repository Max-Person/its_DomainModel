package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.visitors.OperatorVisitor

/**
 * Object литерал
 * @param value Имя объекта
 */
class ObjectLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.Object

    override fun clone(): Operator = ObjectLiteral(value)

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this)
    }
}