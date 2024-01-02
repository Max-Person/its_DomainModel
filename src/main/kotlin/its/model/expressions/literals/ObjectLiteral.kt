package its.model.expressions.literals

import its.model.definition.Domain
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
    val className: String,
) : ReferenceLiteral(name) {

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        //Существование объекта не проверяется, т.к. это динамические данные
        val typeName = if (className.isBlank() && domain.objects.get(name) != null)
            domain.objects.get(name)!!.className
        else className
        val type = ObjectType(typeName)

        results.checkConforming(
            type.exists(domain),
            "No class of name '$typeName' found in domain, " +
                    "but it was declared as a type for object '$name' in $description"
        )
        return type
    }

    override fun clone(): Operator = ObjectLiteral(name, className)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}