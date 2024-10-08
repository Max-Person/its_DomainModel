package its.model.expressions.literals

import its.model.definition.types.BooleanType
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * [BooleanType] литерал
 */
class BooleanLiteral(value: Boolean) : ValueLiteral<Boolean, BooleanType>(value, BooleanType) {

    override fun clone(): Operator = BooleanLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}