package its.model.expressions.operators

import its.model.definition.DomainModel
import its.model.definition.types.AnyType
import its.model.definition.types.BooleanType
import its.model.definition.types.NoneType
import its.model.definition.types.Type
import its.model.expressions.ExpressionContext
import its.model.expressions.ExpressionValidationResults
import its.model.expressions.Operator
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Условный оператор
 * Может иметь полную форму (если-то-иначе), или неполную форму (если-то)
 *
 * В неполной форме ничего не возвращает ([NoneType]).
 * В полной форме возвращает выполняемое выражение - [thenExpr] или [elseExpr] (типизируется как более общее из них)
 * @param conditionExpr условие, определяющее выполнение оператора [thenExpr] ([BooleanType])
 * @param thenExpr выражение, выполняеющееся при выполнении условия ([AnyType])
 * @param elseExpr выражение, выполняеющееся при невыполнении условия ([AnyType]). В неполной форме отсутствует.
 */
class IfThen(
    val conditionExpr: Operator,
    val thenExpr: Operator,
    val elseExpr: Operator? = null,
) : Operator() {
    override val children: List<Operator>
        get() = listOf(conditionExpr, thenExpr)

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        val conditionType = conditionExpr.validateAndGetType(domainModel, results, context)
        results.checkValid(
            conditionType is BooleanType,
            "Condition argument of a $description should be of boolean type, but was '$conditionType'"
        )

        val thenType = thenExpr.validateAndGetType(domainModel, results, context)

        if (elseExpr == null)
            return NoneType

        val elseType = elseExpr.validateAndGetType(domainModel, results, context)
        val thenIsSuper = thenType.castFits(elseType, domainModel)
        val elseIsSuper = elseType.castFits(thenType, domainModel)
        results.checkValid(
            thenIsSuper || elseIsSuper,
            "The alternatives of a $description have incompatible types"
        )

        return if (elseIsSuper)
            elseType
        else //если thenIsSuper, или в случае несовместимых типов
            thenType
    }


    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}