package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.DomainValidationResultsThrowImmediately
import its.model.definition.PropertyDef
import its.model.definition.types.*
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Присвоение значения свойству объекта
 *
 * Ничего не возвращает ([NoneType])
 * @param objectExpr целевой объект ([ObjectType])
 * @param propertyName имя свойства
 * @param valueExpr присваиваемое значение свойства (Тип соответствующий типу свойства)
 */
class AssignProperty(
    val objectExpr: Operator,
    val propertyName: String,
    val valueExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr, valueExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType
        val objType = objectExpr.validateAndGetType(domain, results, context)
        if (objType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $objType")
            return type
        }
        if (!objType.exists(domain)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return type
        }

        val clazz = objType.findIn(domain)
        val propertyOpt = clazz.findPropertyDef(propertyName)
        if (propertyOpt.isEmpty) {
            results.nonConforming(
                "No property '$propertyName' exists for objects of type '${clazz.name}' " +
                        "to be read via $description"
            )
            return type
        }

        val property = propertyOpt.get()
        results.checkConforming(
            property.kind == PropertyDef.PropertyKind.OBJECT,
            "Cannot assign a value to a ${property.description}"
        )

        val propertyType = property.type
        val valueType = valueExpr.validateAndGetType(domain, results, context)
        results.checkConforming(
            propertyType.castFits(valueType, domain),
            "Cannot assign a value of type $valueType to a property of type $propertyType"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}