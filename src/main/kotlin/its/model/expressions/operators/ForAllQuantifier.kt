package its.model.expressions.operators

import its.model.TypedVariable
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
 * Квантор общности ("Для всех ...")
 * Может использоваться также в качестве цикла по объектам модели
 *
 * Возвращает [BooleanType], если [conditionExpr] имеет [BooleanType] (Агрегирует результаты по логическому И);
 * Иначе ничего не возвращает ([NoneType])
 * @param variable контекстная переменная, задающая ссылку на проверяемый объект
 * @param selectorExpr условие, задающее область определения переменной ([BooleanType]);
 *      Может отсутствовать, в таком случае область определения считается равной всем объектам заданного типа.
 * @param conditionExpr выражение, определяющее действия с переменной в области определения ([AnyType])
 */
class ForAllQuantifier(
    val variable: TypedVariable,
    val selectorExpr: Operator? = null,
    val conditionExpr: Operator,
) : Operator() {

    override val children: List<Operator>
        get() = listOf(selectorExpr, conditionExpr).filterNotNull()

    override fun validateAndGetType(
        domainModel: DomainModel,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        variable.checkValid(domainModel, results, context, this)

        context.add(variable)
        if (selectorExpr != null) {
            val selectorType = selectorExpr.validateAndGetType(domainModel, results, context)
            results.checkValid(
                selectorType is BooleanType,
                "Selector argument of $description should be of boolean type, but was '$selectorType'"
            )
        }
        val conditionType = conditionExpr.validateAndGetType(domainModel, results, context)
        context.remove(variable)

        return if (conditionType is BooleanType)
            BooleanType
        else
            NoneType
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}