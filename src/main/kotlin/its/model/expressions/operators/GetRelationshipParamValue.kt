package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.NoneType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить значение параметра связи между объектами по отношению.
 * Во многом аналогичен [CheckRelationship], но не использует проекцию.
 * Если под критерии поиска связи подходит несколько связей, то выкидывается ошибка.
 *
 * Возвращает тип, соответствующий типу указанного параметра
 * @param subjectExpr исходный объект (субъект) искомой связи ([ObjectType])
 * @param relationshipName имя отношения
 * @param paramsValues значения параметров, с которыми сопоставляется искомая связь. Могут быть описаны полностью или частично
 * @param objectExprs выходные объекты искомой связи (Все [ObjectType])
 * @param paramName имя параметра, чье значение получается в этом выражении
 */
class GetRelationshipParamValue(
    val subjectExpr: Operator,
    val relationshipName: String,
    val paramsValues: ParamsValuesExprList = ParamsValuesExprList.EMPTY,
    val objectExprs: List<Operator>,
    val paramName: String,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(subjectExpr).plus(objectExprs).plus(paramsValues.getExprList())

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = NoneType

        val objectTypes = objectExprs.map { it.validateAndGetType(domainModel, results, context) }
        val areAllObjectsOfObjectType = objectTypes.all { it is ObjectType }
        results.checkValid(
            areAllObjectsOfObjectType,
            "All object-arguments of $description should be objects, " +
                    "but were ${objectTypes.joinToString(", ")}"
        )

        val subjType = subjectExpr.validateAndGetType(domainModel, results, context)
        if (subjType !is ObjectType) {
            results.invalid("Subject-argument of $description should be an object, but was $subjType")
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
                        "to be read via $description." +
                        " Keep in mind that getting relationship params cannot be done with projected relationships."
            )
            return invalidType
        }

        paramsValues.validatePartial(relationship.effectiveParams, this, domainModel, results, context)
        results.checkConforming(
            relationship.effectiveParams.containsKey(paramName),
            "No parameter with name '$paramName' exists for $relationship to be read via $description"
        )
        val type = relationship.effectiveParams[paramName]?.type ?: invalidType

        val isCorrectObjectCount = objectExprs.size == relationship.objectClassNames.size
        results.checkConforming(
            isCorrectObjectCount,
            "${relationship.description} links a subject to ${relationship.objectClassNames.size} objects, " +
                    "so $description needs ${relationship.objectClassNames.size} object-arguments, " +
                    "but ${objectExprs.size} were provided"
        )
        if (!areAllObjectsOfObjectType || !isCorrectObjectCount) return type

        for ((i, objectType) in objectTypes.withIndex()) {
            val expectedClassName = relationship.objectClassNames[i]
            val expectedType = ObjectType(expectedClassName)
            objectType as ObjectType
            results.checkConforming(
                expectedType.castFits(objectType, domainModel) || expectedType.projectFits(objectType, domainModel),
                "Link object at index $i in a ${relationship.description} " +
                        "is expected to be of type $expectedClassName, but was ${objectType.className}; " +
                        "No valid projections available (in $description)"
            )
        }

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}