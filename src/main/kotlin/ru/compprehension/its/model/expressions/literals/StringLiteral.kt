package ru.compprehension.its.model.expressions.literals

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * String литерал
 * @param value Значение
 */
class StringLiteral(value: String) : ValueLiteral<String>(value) {

    override val resultDataType: KClass<String>
        get() = Types.String

    override fun clone(): Operator = StringLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}