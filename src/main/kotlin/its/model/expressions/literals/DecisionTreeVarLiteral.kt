package its.model.expressions.literals

import its.model.definition.DomainModel
import its.model.definition.types.AnyType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Литерал переменной дерева решений.
 *
 * Узлы дерева вводят в дерево переменные-оъекты, и поэтому в большинстве случаев они возвращают [ObjectType],
 * однако в общем система позволяет задавать переменные любого типа, поэтому в общем случае они возвращают [AnyType].
 * @param name имя переменной
 */
class DecisionTreeVarLiteral(name: String) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = ObjectType.untyped()
        if (!context.decisionTreeVariableTypes.containsKey(name)) {
            results.invalid("No variable '$name' is known to get value from in $description")
            return invalidType
        }
        return context.decisionTreeVariableTypes[name]!!
    }

    override fun clone(): Operator = VariableLiteral(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}