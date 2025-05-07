package its.model.nodes

import its.model.TypedVariable
import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.expressions.Operator

/**
 * Категория ошибок в узле [FindActionNode]
 *
 * В случае, если студент неправильно выполнил действие в узле и выбрал не тот объект, который нужно,
 * то любой выбранный объект должен соответствовать одной или нескольким категориям ошибок
 *
 * @param priority приоритет данной категории ошибок - если объект соответствует нескольким категориям,
 * то будет выбрана категория с наивысшим приоритетом (1 считается наивысшим приоритетом, и далее 2, 3.. по убыванию)
 * @param selectorExpr условие (предикат) для проверки соответствия объекта категории ([BooleanType]);
 * проверяемый объект подставляется в предикат как контекстная переменная 'checked', чей тип соответствует основной переменной узла
 */
class FindErrorCategory(
    val priority: Int,
    val selectorExpr: Operator,
    val checkedVariable: TypedVariable,
) : HelperDecisionTreeElement() {

    companion object {
        const val CHECKED_OBJ = "checked"
    }

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        super.validate(domainModel, results, context)
        val selectorType = selectorExpr.validateForDecisionTree(
            domainModel,
            results,
            context,
            withVariables = listOf(checkedVariable)
        )
        results.checkValid(
            selectorType is BooleanType,
            "Selector expression in a $description returns $selectorType, but must return a boolean " +
                    "(be a predicate with respect to variable '$CHECKED_OBJ')"
        )
    }
}