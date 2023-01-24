package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.ComparisonResult
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil

/**
 * Литерал результата сравнения
 * @param value Результат сравнения
 * @see ComparisonResult
 */
class ComparisonResultLiteral(value: ComparisonResult) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.ComparisonResult

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genLink(JenaUtil.POAS_PREF, value))

    override fun clone(): Operator = ComparisonResultLiteral(ComparisonResult.valueOf(value)!!)
}