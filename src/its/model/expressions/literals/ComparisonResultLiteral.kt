package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.ComparisonResult
import its.model.util.DataType

/**
 * Литерал результата сравнения
 * @param value Результат сравнения
 * @see ComparisonResult
 */
class ComparisonResultLiteral(value: ComparisonResult) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.ComparisonResult

    override fun clone(): Operator = ComparisonResultLiteral(ComparisonResult.valueOf(value)!!)
}