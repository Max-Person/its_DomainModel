package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.ComparisonResult
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Литерал результата сравнения
 * @param value Результат сравнения
 * @see ComparisonResult
 */
class ComparisonResultLiteral(value: ComparisonResult) : ValueLiteral<ComparisonResult>(value) {

    override val resultDataType: KClass<ComparisonResult>
        get() = Types.ComparisonResult

    override fun clone(): Operator = ComparisonResultLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}