package its.model.expressions.literals

import its.model.definition.Domain
import its.model.definition.types.ClassType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Литерал ссылки на класс ([ClassType])
 * @param name имя класса
 */
class ClassLiteral(name: String) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = ClassType(name)
        results.checkConforming(
            type.exists(domain),
            "No class of name '$name' found in domain, " +
                    "but it is used in $description"
        )
        return type
    }

    override fun clone(): Operator = ClassLiteral(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}