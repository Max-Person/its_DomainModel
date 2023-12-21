package its.model.expressions.literals

import its.model.definition.types.IntegerType
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * [IntegerType] литерал
 */
class IntegerLiteral(value: Int) : ValueLiteral<Int, IntegerType>(value, IntegerType(value)) {

    override fun clone(): Operator = IntegerLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}