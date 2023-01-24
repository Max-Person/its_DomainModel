package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil

/**
 * Boolean литерал
 * @param value Значение
 */
class BooleanLiteral(value: Boolean) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.Boolean

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genVal(value.toBoolean()))

    /**
     * Скомпилировать boolean литерал как head
     * @return Голова правила, прерывающая правило, если значение false
     */
    fun compileAsHead() =
        if (value.toBoolean()) JenaUtil.genEqualPrim("1", "1")
        else JenaUtil.genEqualPrim("0", "1")

    override fun clone(): Operator = BooleanLiteral(value.toBoolean())
}