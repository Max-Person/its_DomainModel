package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Boolean литерал
 * @param value Значение
 */
class BooleanLiteral(value: Boolean) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Boolean


    override fun clone(): Operator = BooleanLiteral(value.toBoolean())
}