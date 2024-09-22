package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.ClassType
import its.model.definition.types.ObjectType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить непосредственный класс объекта
 *
 * Возвращает [ClassType]
 * @param objectExpr объект, у которого берется класс ([ObjectType])
 */
class GetClass(
    val objectExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(objectExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val objType = objectExpr.validateAndGetType(domainModel, results, context)
        if (objType !is ObjectType) {
            results.invalid("Argument of $description should be an object, but was $objType")
            return ClassType.untyped()
        }

        return objType.toClassType()
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}