package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.definition.types.ClassType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Проверить, является ли объект наследником класса
 *
 * Возвращает [BooleanType]
 * @param objectExpr целевой объект ([ObjectType])
 * @param classExpr проверяемый класс ([ClassType])
 */
class CheckClass(
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
        val type = BooleanType

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

        if (!objIsOfObjectType || !classIsOfClassType) return type

        objType as ObjectType
        classType as ClassType
        if (!objType.exists(domainModel) || !classType.exists(domainModel)) return type

        val objClassType = objType.toClassType()
        results.checkConforming(
            objClassType.castFits(classType, domainModel),
            "$description checks for a class ${classType.className} on an object " +
                    "of a non-compatible type '${objType.className}'"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}