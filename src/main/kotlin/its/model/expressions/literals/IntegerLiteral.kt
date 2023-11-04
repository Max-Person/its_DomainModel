package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Integer литерал
 * @param value Значение
 */
class IntegerLiteral(value: Int) : ValueLiteral<Int>(value) {

    override val resultDataType: KClass<Int>
        get() = Types.Integer

    override fun clone(): Operator = IntegerLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}