package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
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