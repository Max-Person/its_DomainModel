package its.model.expressions.literals

import its.model.definition.DomainModel
import its.model.definition.types.AnyType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Контекстная переменная, вводимая некоторыми операторами.
 *
 * Операторы вводят контекстные переменные-объекты, и поэтому в большинстве случаев они возвращают [ObjectType],
 * однако в общем система позволяет задавать переменные любого типа, поэтому в общем случае они возвращают [AnyType].
 * @param name Имя переменной
 */
class VariableLiteral(
    name: String
) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = ObjectType.untyped()
        if (!context.variableTypes.containsKey(name)) {
            results.invalid("No variable '$name' is known to get value from in $description")
            return invalidType
        }
        return context.variableTypes[name]!!
    }

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}