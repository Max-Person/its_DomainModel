package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Double литерал
 * @param value Значение
 */
class DoubleLiteral(value: Double) : ValueLiteral<Double>(value) {

    override val resultDataType: KClass<Double>
        get() = Types.Double

    override fun clone(): Operator = DoubleLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}