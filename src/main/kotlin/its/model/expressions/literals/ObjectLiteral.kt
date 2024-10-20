package its.model.expressions.literals

import its.model.definition.DomainModel
import its.model.definition.ObjectRef
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Object литерал ([ObjectType])
 * @param name имя объекта
 */
class ObjectLiteral(
    name: String,
) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        //Существование объекта
        val obj = ObjectRef(name).findIn(domainModel)
        results.checkConforming(
            obj != null,
            "No object named '$name' found in domain, " +
                    "so it cannot be used in $description"
        )
        return obj?.let { ObjectType(it.className) } ?: ObjectType.untyped()
    }

    override fun clone(): Operator = ObjectLiteral(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}