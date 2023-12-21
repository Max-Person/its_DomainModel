package its.model.expressions.operators

import its.model.definition.ClassRef
import its.model.definition.Domain
import its.model.definition.DomainValidationResultsThrowImmediately
import its.model.definition.types.*
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить значение свойства объекта
 *
 * Возвращает тип соответствующий типу свойства
 * @param objectExpr целевой объект ([ObjectType])
 * @param propertyName имя свойства
 */
class GetPropertyValue(
    val objectExpr: Operator,
    val propertyName: String,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = AnyType
        val objType = objectExpr.validateAndGetType(domain, results, context)
        if (objType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $objType")
            return invalidType
        }
        if (!objType.exists(domain)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return invalidType
        }

        val clazz = objType.findIn(domain)
        val propertyOpt = clazz.findPropertyDef(propertyName)
        if (propertyOpt.isEmpty) {
            results.nonConforming(
                "No property '$propertyName' exists for objects of type '${clazz.name}' " +
                        "to be read via $description"
            )
            return invalidType
        }

        return propertyOpt.get().type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}