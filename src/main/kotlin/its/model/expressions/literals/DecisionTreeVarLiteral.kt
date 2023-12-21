package its.model.expressions.literals

import its.model.definition.Domain
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Литерал переменной дерева решений ([ObjectType])
 * @param name имя переменной
 */
class DecisionTreeVarLiteral(name: String) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = ObjectType.untyped()
        if (!context.decisionTreeVariableTypes.containsKey(name)) {
            results.invalid("No variable '$name' is known to get value from in $description")
            return invalidType
        }
        return ObjectType(context.decisionTreeVariableTypes[name]!!)
    }

    override fun clone(): Operator = VariableLiteral(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}