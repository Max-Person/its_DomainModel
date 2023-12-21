package its.model.expressions.literals

import its.model.definition.types.DoubleType
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * [DoubleType] литерал
 */
class DoubleLiteral(value: Double) : ValueLiteral<Double, DoubleType>(value, DoubleType(value)) {

    override fun clone(): Operator = DoubleLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}