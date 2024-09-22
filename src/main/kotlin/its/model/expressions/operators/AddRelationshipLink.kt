package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.NoneType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Добавить объектам связь по отношению
 *
 * Ничего не возвращает ([NoneType])
 * @param subjectExpr исходный объект (субъект) проставляемой связи ([ObjectType])
 * @param relationshipName имя отношения
 * @param objectExprs выходные объекты проставляемой связи (Все [ObjectType])
 */
class AddRelationshipLink(
    val subjectExpr: Operator,
    val relationshipName: String,
    val objectExprs: List<Operator>,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(subjectExpr).plus(objectExprs)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        //FIXME большой повтор кода относительно CheckRelationship
        val type = NoneType

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
        val relationship = clazz.findRelationshipDef(relationshipName)
        if (relationship == null) {
            results.nonConforming(
                "No relationship '$relationshipName' exists for objects of type '${clazz.name}' " +
                        "to be added via $description"
            )
            return type
        }

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
                expectedType.castFits(objectType, domainModel),
                "Link object at index $i in a ${relationship.description} " +
                        "is expected to be of type $expectedClassName, but was ${objectType.className};"
            )
        }

        return type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}