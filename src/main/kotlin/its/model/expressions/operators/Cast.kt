package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.ClassType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Преобразовать объект к классу
 *
 * Возвращает [ObjectType] (с типом, соответствующим [classExpr])
 * @param objectExpr преобразуемый объект ([ObjectType])
 * @param classExpr целевой класс ([ClassType])
 */
class Cast(
    val objectExpr: Operator,
    val classExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr, classExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val objType = objectExpr.validateAndGetType(domainModel, results, context)
        val objIsOfObjectType = objType is ObjectType
        results.checkValid(
            objIsOfObjectType,
            "Object-argument of $description should be an object, but was $objType"
        )

        val classType = classExpr.validateAndGetType(domainModel, results, context)
        val classIsOfClassType = classType is ClassType
        results.checkValid(
            classIsOfClassType,
            "Class-argument of $description should be a class, but was $objType"
        )

        if (!classIsOfClassType) return ObjectType.untyped()

        val type = (classType as ClassType).toObjectType()
        if (!objIsOfObjectType) return type

        objType as ObjectType
        if (!objType.exists(domainModel) || !classType.exists(domainModel)) return type

        results.checkConforming(
            objType.castFits(type, domainModel),
            "$description casts an object of type '${objType.className}' to type '${classType.className}'," +
                    "which can never succeed, as '${classType.className}' is not a subtype of '${objType.className}'"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}