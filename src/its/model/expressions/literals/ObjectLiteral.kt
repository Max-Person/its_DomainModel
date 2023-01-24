package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Object литерал
 * @param value Имя объекта
 */
class ObjectLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.Object

    override fun clone(): Operator = ObjectLiteral(value)
}