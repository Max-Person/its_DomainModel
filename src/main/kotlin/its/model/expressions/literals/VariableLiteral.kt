package its.model.expressions.literals

import its.model.definition.Domain
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Контекстная переменная, вводимая некоторыми операторами ([ObjectType])
 * @param name Имя переменной
 */
class VariableLiteral(
    name: String
) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = ObjectType.untyped()
        if (!context.variableTypes.containsKey(name)) {
            results.invalid("No variable '$name' is known to get value from in $description")
            return invalidType
        }
        return ObjectType(context.variableTypes[name]!!)
    }

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}