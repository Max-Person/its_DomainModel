package its.model.expressions.literals

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.ComparisonResult
import its.model.expressions.types.DataType
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Литерал результата сравнения
 * @param value Результат сравнения
 * @see ComparisonResult
 */
class ComparisonResultLiteral(value: ComparisonResult) : Literal(value.toString()) {

    override val resultDataType: DataType
        get() = DataType.ComparisonResult

    override fun clone(): Operator = ComparisonResultLiteral(ComparisonResult.fromString(value)!!)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}