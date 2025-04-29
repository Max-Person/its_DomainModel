package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.Comparison
import its.model.definition.types.EnumType
import its.model.definition.types.NumericType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Трехзначное сравнение (больше/меньше/равно)
 *
 * Возвращает [EnumType] сравнения ([Comparison.Type])
 * @param firstExpr первое сравниваемое значение ([NumericType])
 * @param secondExpr второе сравниваемое значение ([NumericType])
 */
class Compare(
    val firstExpr: Operator,
    val secondExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(firstExpr, secondExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val firstType = firstExpr.validateAndGetType(domainModel, results, context)
        val secondType = secondExpr.validateAndGetType(domainModel, results, context)
        results.checkValid(
            firstType is NumericType && secondType is NumericType,
            "$description is not compatible with non-numeric types " +
                    "(trying to compare values of types '$firstType' and '$secondType')"
        )
        return Comparison.Type
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}