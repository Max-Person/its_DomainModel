package its.model.expressions.operators

import its.model.TypedVariable
import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.Type
import its.model.expressions.*
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Квантор существования (Возвращает [BooleanType])
 *
 * Возвращает [BooleanType]
 * @param variable контекстная переменная, задающая ссылку на проверяемый объект
 * @param selectorExpr условие, задающее область определения переменной ([BooleanType])
 * @param conditionExpr условие, предъявляемое к переменной в области определения ([BooleanType])
 */
class ExistenceQuantifier(
    val variable: TypedVariable,
    val selectorExpr: Operator,
    val conditionExpr: Operator,
    private var isNegative: Boolean = false,
) : Operator(), HasNegativeForm, HasContext {

    val mContext = HashSet<String>()

    override val context: MutableSet<String>
        get() = mContext

    override fun isNegative(): Boolean = isNegative

    override fun setIsNegative(isNegative: Boolean) {
        this.isNegative = isNegative
    }

    override val children: List<Operator>
        get() = listOf(selectorExpr, conditionExpr)

    override fun validateAndGetType(
        domain: Domain,
        results: ExpressionValidationResults,
        context: ExpressionContext
    ): Type<*> {
        variable.checkValid(domain, results, context, this)

        context.variableTypes[variable.varName] = variable.className
        val selectorType = selectorExpr.validateAndGetType(domain, results, context)
        results.checkValid(
            selectorType is BooleanType,
            "Selector argument of $description should be of boolean type, but was '$selectorType'"
        )
        val conditionType = conditionExpr.validateAndGetType(domain, results, context)
        results.checkValid(
            conditionType is BooleanType,
            "Condition argument of $description should be of boolean type, but was '$conditionType'"
        )
        context.variableTypes.remove(variable.varName)

        return BooleanType
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }

    override fun clone(): Operator =
        ExistenceQuantifier(variable, selectorExpr.clone(), conditionExpr.clone(), isNegative).also {
            it.mContext.addAll(mContext)
        }

    override fun clone(newArgs: List<Operator>): Operator = ExistenceQuantifier(variable, newArgs.first(), newArgs.last(), isNegative).also {
        it.mContext.addAll(mContext)
    }
}