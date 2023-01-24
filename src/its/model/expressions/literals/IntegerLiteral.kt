package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Integer литерал
 * @param value Значение
 */
class IntegerLiteral(value: Int) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Integer

    override fun clone(): Operator = IntegerLiteral(value.toInt())
}