package ru.compprehension.its.model.expressions.literals

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Boolean литерал
 * @param value Значение
 */
class BooleanLiteral(value: Boolean) : ValueLiteral<Boolean>(value) {

    override val resultDataType: KClass<Boolean>
        get() = Types.Boolean


    override fun clone(): Operator = BooleanLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}