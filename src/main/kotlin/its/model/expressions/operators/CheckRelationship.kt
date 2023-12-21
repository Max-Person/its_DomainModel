package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.DomainValidationResultsThrowImmediately
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Проверка наличия связи по отношению между объектами (с учетом вычисляемых отношений и проекции)
 *
 * Возвращает [BooleanType]
 * @param subjectExpr исходный объект (субъект) проверяемой связи ([ObjectType])
 * @param relationshipName имя отношения
 * @param objectExprs выходные объекты проверямой связи (Все [ObjectType])
 */
class CheckRelationship(
    val subjectExpr: Operator,
    val relationshipName: String,
    val objectExprs: List<Operator>,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(subjectExpr).plus(objectExprs)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val type = BooleanType

        val objectTypes = objectExprs.map { it.validateAndGetType(domain, results, context) }
        val areAllObjectsOfObjectType = objectTypes.all { it is ObjectType }
        results.checkValid(
            areAllObjectsOfObjectType,
            "All object-arguments of $description should be objects, " +
                    "but were ${objectTypes.joinToString(", ")}"
        )

        val subjType = subjectExpr.validateAndGetType(domain, results, context)
        if (subjType !is ObjectType) {
            results.invalid("Subject-argument of $description should be an object, but was $subjType")
            return type
        }
        if (!subjType.exists(domain)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return type
        }

        val clazz = subjType.findIn(domain)
        var relationshipOpt = clazz.findRelationshipDef(relationshipName)
        if (relationshipOpt.isEmpty) {
            val possibleRelationships = clazz.projectionClasses
                .map { it.findRelationshipDef(relationshipName) }
                .filter { it.isPresent }
            if (possibleRelationships.isEmpty()) {
                results.nonConforming(
                    "No relationship '$relationshipName' exists for objects of type '${clazz.name}' " +
                            "to be read via $description"
                )
                return type
            }
            if (possibleRelationships.size >= 2) {
                results.nonConforming(
                    "Multiple relationship definitions for name '$relationshipName' are available to check " +
                            "for objects of type '${clazz.name}' via projection in $description: " +
                            possibleRelationships.map { it.get() }.joinToString(", ")
                )
                return type
            }
            relationshipOpt = possibleRelationships.single()
        }

        val relationship = relationshipOpt.get()
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
            if (objectType.exists(domain)) {
                if (!expectedType.castFits(objectType, domain) && !expectedType.projectFits(objectType, domain))
                    results.checkConforming(
                        ObjectType(expectedClassName).castFits(objectType, domain),
                        "Link object at index $i in a ${relationship.description} " +
                                "is expected to be of type $expectedClassName, but was ${objectType.className}; " +
                                "No valid projections available (in $description)"
                    )
            }
        }

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}