package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.PropertyDef
import its.model.definition.types.BooleanType
import its.model.definition.types.NoneType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Присвоение значения свойству объекта
 *
 * Ничего не возвращает ([NoneType])
 * @param objectExpr целевой объект ([ObjectType])
 * @param propertyName имя свойства
 * @param paramsValues значения параметров, для которых проставляется значение свойства
 *      (должны быть прописаны все параметры, определяемые свойством)
 * @param valueExpr присваиваемое значение свойства (Тип соответствующий типу свойства)
 */
class AssignProperty(
    val objectExpr: Operator,
    val propertyName: String,
    val paramsValues: ParamsValuesExprList = ParamsValuesExprList.EMPTY,
    val valueExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr, valueExpr).plus(paramsValues.getExprList())

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType
        val objType = objectExpr.validateAndGetType(domainModel, results, context)
        if (objType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $objType")
            return type
        }
        if (!objType.exists(domainModel)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return type
        }

        val clazz = objType.findIn(domainModel)
        val property = clazz.findPropertyDef(propertyName)
        if (property == null) {
            results.nonConforming(
                "No property '$propertyName' exists for objects of type '${clazz.name}' " +
                        "to be assigned via $description"
            )
            return type
        }

        paramsValues.validateFull(property.paramsDecl, this, domainModel, results, context)

        results.checkConforming(
            property.kind == PropertyDef.PropertyKind.OBJECT,
            "Cannot assign a value to a ${property.description}"
        )

        val propertyType = property.type
        val valueType = valueExpr.validateAndGetType(domainModel, results, context)
        results.checkConforming(
            propertyType.castFits(valueType, domainModel),
            "Cannot assign a value of type $valueType to a property of type $propertyType"
        )

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}