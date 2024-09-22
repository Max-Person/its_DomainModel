package its.model.expressions.literals

import its.model.definition.DomainModel
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
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        //Существование объекта не проверяется, т.к. это динамические данные
        val typeName = if (className.isBlank() && domainModel.objects.get(name) != null)
            domainModel.objects.get(name)!!.className
        else className
        val type = ObjectType(typeName)

        results.checkConforming(
            type.exists(domainModel),
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