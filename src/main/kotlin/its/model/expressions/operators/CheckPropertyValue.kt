package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.AnyType
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Проверить, имеет ли свойство объекта искомое значение
 *
 * Возвращает [BooleanType]
 * @param objectExpr целевой объект ([ObjectType])
 * @param propertyName имя свойства
 * @param valueExpr проверяемое значение (Тип соответствующий типу свойства)
 */
class CheckPropertyValue(
    val objectExpr: Operator,
    val propertyName: String,
    val valueExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr, valueExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType
        //т.к. CheckPropertyValue это синтаксический сахар, то валидация такая же как и у GetPropertyValue
        val propertyType = GetPropertyValue(objectExpr, propertyName).validateAndGetType(domainModel, results, context)
        if (propertyType is AnyType) return type

        val valueType = valueExpr.validateAndGetType(domainModel, results, context)
        results.checkConforming(
            propertyType.castFits(valueType, domainModel),
            "$description checks for a value of type $valueType in a property $propertyName, " +
                    "which has a non-compatible type '$propertyType'"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}