package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * String литерал
 * @param value Значение
 */
class StringLiteral(value: String) : Literal(value) {

    override val resultDataType: DataType
        get() = DataType.String

    override fun clone(): Operator = StringLiteral(value)
}