package its.model.expressions.operators

import its.model.definition.Domain
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить объект по отношению
 *
 * Возвращает [ObjectType], выбрасывает ошибку, если объекта нет
 * @param subjectExpr исходный объект (субъект) проверяемой связи ([ObjectType])
 * @param relationshipName имя отношения
 */
class GetByRelationship(
    val subjectExpr: Operator,
    val relationshipName: String,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(subjectExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val invalidType = ObjectType.untyped()
        val subjType = subjectExpr.validateAndGetType(domain, results, context)
        if (subjType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $subjType")
            return invalidType
        }
        if (!subjType.exists(domain)) {
            //Если невалидный класс, это кинется где-то ниже (где этот тип создавался)
            return invalidType
        }

        val clazz = subjType.findIn(domain)
        val relationship = clazz.findRelationshipDef(relationshipName)
        if (relationship == null) {
            results.nonConforming(
                "No relationship '$relationshipName' exists for objects of type '${clazz.name}' " +
                        "to be read via $description"
            )
            return invalidType
        }

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