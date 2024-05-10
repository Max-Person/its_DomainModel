package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.AnyType
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.HasNegativeForm
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
    private var isNegative: Boolean = false,
) : Operator(), HasNegativeForm {

    override fun isNegative(): Boolean = isNegative

    override fun setIsNegative(isNegative: Boolean) {
        this.isNegative = isNegative
    }

    override val children: List<Operator>
        get() = listOf(objectExpr, valueExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType
        //т.к. CheckPropertyValue это синтаксический сахар, то валидация такая же как и у GetPropertyValue
        val propertyType = GetPropertyValue(objectExpr, propertyName).validateAndGetType(domain, results, context)
        if (propertyType is AnyType) return type

        val valueType = valueExpr.validateAndGetType(domain, results, context)
        results.checkConforming(
            propertyType.castFits(valueType, domain),
            "$description checks for a value of type $valueType in a property $propertyName, " +
                    "which has a non-compatible type '$propertyType'"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }

    override fun clone(): Operator = CheckPropertyValue(objectExpr.clone(), propertyName, valueExpr.clone(), isNegative)

    override fun clone(newArgs: List<Operator>): Operator = CheckPropertyValue(newArgs.first(), propertyName, newArgs.last(), isNegative)
}