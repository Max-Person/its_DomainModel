package its.model.expressions.operators

import its.model.definition.ClassDef
import its.model.definition.DomainModel
import its.model.definition.RelationshipDef
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.utils.ParamsValuesExprList
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Проверка наличия связи по отношению между объектами (с учетом вычисляемых отношений и проекции)
 *
 * Возвращает [BooleanType]
 * @param subjectExpr исходный объект (субъект) проверяемой связи ([ObjectType])
 * @param relationshipName имя отношения
 * @param paramsValues значения параметров, с которыми сопоставляется искомая связь. Могут быть описаны полностью или частично
 * @param objectExprs выходные объекты проверямой связи (Все [ObjectType])
 */
class CheckRelationship(
    val subjectExpr: Operator,
    val relationshipName: String,
    val paramsValues: ParamsValuesExprList = ParamsValuesExprList.EMPTY,
    val objectExprs: List<Operator>,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(subjectExpr).plus(objectExprs).plus(paramsValues.getExprList())

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType

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
            return type
        }
        if (!subjType.exists(domainModel)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return type
        }

        val clazz = subjType.findIn(domainModel)
        val relationship = getRelationship(clazz, results) ?: return type

        paramsValues.validatePartial(relationship.effectiveParams, this, domainModel, results, context)

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

    private fun getRelationship(
        subjClass: ClassDef,
        results: ExpressionValidationResults,
    ): RelationshipDef? {
        var relationship = subjClass.findRelationshipDef(relationshipName)
        if (relationship == null) {
            val possibleRelationships =
                subjClass.projectionClasses.mapNotNull { it.findRelationshipDef(relationshipName) }
            if (possibleRelationships.isEmpty()) {
                results.nonConforming(
                    "No relationship '$relationshipName' exists for objects of type '${subjClass.name}' " +
                            "to be read via $description"
                )
                return null
            }
            if (possibleRelationships.size >= 2) {
                results.nonConforming(
                    "Multiple relationship definitions for name '$relationshipName' are available to check " +
                            "for objects of type '${subjClass.name}' via projection in $description: " +
                            possibleRelationships.joinToString(", ")
                )
                return null
            }
            relationship = possibleRelationships.single()
        }
        return relationship
    }

    /**
     * Получить отношение с учетом проекции
     */
    fun getRelationship(subjClass: ClassDef) =
        getRelationship(subjClass, ExpressionValidationResults(true))!!

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}