package ru.compprehension.its.model.expressions.literals

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.ComparisonResult
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.LiteralBehaviour
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