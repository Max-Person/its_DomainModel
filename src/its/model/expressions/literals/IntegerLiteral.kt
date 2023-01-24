package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil

/**
 * Integer литерал
 * @param value Значение
 */
class IntegerLiteral(value: Int) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Integer

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genVal(value.toInt()))

    override fun clone(): Operator = IntegerLiteral(value.toInt())
}