package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil

/**
 * Double литерал
 * @param value Значение
 */
class DoubleLiteral(value: Double) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Double

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genVal(value.toDouble()))

    override fun clone(): Operator = DoubleLiteral(value.toDouble())
}