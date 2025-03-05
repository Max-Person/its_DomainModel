package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить объект, с которым субъект связан по заданному отношению.
 * Если таких связей много, то выкидывается ошибка
 *
 * Возвращает [ObjectType], выбрасывает ошибку, если объекта нет
 * @param subjectExpr исходный объект (субъект) искомой связи ([ObjectType])
 * @param relationshipName имя отношения
 * @param paramsValues значения параметров, с которыми сопоставляется искомая связь. Могут быть описаны полностью или частично
 */
class GetByRelationship(
    val subjectExpr: Operator,
    val relationshipName: String,
    val paramsValues: ParamsValuesExprList = ParamsValuesExprList.EMPTY,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(subjectExpr).plus(paramsValues.getExprList())

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = ObjectType.untyped()
        val subjType = subjectExpr.validateAndGetType(domainModel, results, context)
        if (subjType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $subjType")
            return invalidType
        }
        if (!subjType.exists(domainModel)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return invalidType
        }

        val clazz = subjType.findIn(domainModel)
        val relationship = clazz.findRelationshipDef(relationshipName)
        if (relationship == null) {
            results.nonConforming(
                "No relationship '$relationshipName' exists for objects of type '${clazz.name}' " +
                        "to be read via $description"
            )
            return invalidType
        }

        paramsValues.validatePartial(relationship.effectiveParams, this, domainModel, results, context)

        if (!relationship.isBinary) {
            results.invalid(
                "Non-binary relationship '$relationshipName' " +
                        "cannot be used with $description, " +
                        "as the object part of the link statement contains multiple objects"
            )
            return invalidType
        }

        return ObjectType(relationship.objectClassNames.first())
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}