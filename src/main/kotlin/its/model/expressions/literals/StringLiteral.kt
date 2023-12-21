package its.model.expressions.literals

import its.model.definition.types.StringType
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * [StringType] литерал
 */
class StringLiteral(value: String) : ValueLiteral<String, StringType>(value, StringType) {

    override fun clone(): Operator = StringLiteral(value)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}