package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.PropertyDef
import its.model.definition.types.AnyType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить значение свойства объекта
 *
 * Возвращает тип соответствующий типу свойства
 * @param objectExpr целевой объект ([ObjectType])
 * @param propertyName имя свойства
 * @param paramsValues значения параметров, для которых получается значение свойства
 *      (должны быть прописаны все параметры, определяемые свойством)
 */
class GetPropertyValue(
    val objectExpr: Operator,
    val propertyName: String,
    val paramsValues: ParamsValuesExprList = ParamsValuesExprList.EMPTY,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr).plus(paramsValues.getExprList())

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = AnyType
        val objType = objectExpr.validateAndGetType(domainModel, results, context)
        if (objType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $objType")
            return invalidType
        }
        if (!objType.exists(domainModel)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return invalidType
        }

        val clazz = objType.findIn(domainModel)
        val property = clazz.findPropertyDef(propertyName)
        if (property == null) {
            results.nonConforming(
                "No property '$propertyName' exists for objects of type '${clazz.name}' " +
                        "to be read via $description"
            )
            return invalidType
        }

        paramsValues.validateFull(property.paramsDecl, this, domainModel, results, context)

        return property.type
    }

    fun getPropertyDef(domainModel: DomainModel): PropertyDef {
        val objectType = objectExpr.resolvedType(domainModel) as ObjectType
        val classDef = objectType.findIn(domainModel)
        return classDef.findPropertyDef(propertyName)!!
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}