package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.*
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
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType

        val objType = objectExpr.validateAndGetType(domain, results, context)
        val objIsOfObjectType = objType is ObjectType
        results.checkValid(
            objIsOfObjectType,
            "Object-argument of $description should be an object, but was $objType"
        )

        val classType = classExpr.validateAndGetType(domain, results, context)
        val classIsOfClassType = classType is ClassType
        results.checkValid(
            classIsOfClassType,
            "Class-argument of $description should be a class, but was $objType"
        )

        if (!objIsOfObjectType || !classIsOfClassType) return type

        objType as ObjectType
        classType as ClassType
        if (!objType.exists(domain) || !classType.exists(domain)) return type

        val objClassType = objType.toClassType()
        results.checkConforming(
            objClassType.castFits(classType, domain),
            "$description checks for a class ${classType.className} on an object " +
                    "of a non-compatible type '${objType.className}'"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}