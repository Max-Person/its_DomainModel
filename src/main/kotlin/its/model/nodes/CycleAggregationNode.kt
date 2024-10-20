package its.model.nodes

import its.model.TypedVariable
import its.model.definition.DomainModel
import its.model.definition.types.BooleanType
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел циклической агрегации
 *
 * Выполняет ветку [thoughtBranch] для каждого объекта соответствующего условию [selectorExpr]
 * (внутри ветки к объекту можно обращаться с помощью переменной [variable])
 * и агрегирует результаты ветви по методу [aggregationMethod];
 * Дальнейшие переходы осуществляются в зависимости от результата агрегации
 *
 * @see WhileCycleNode
 *
 * @param aggregationMethod метод агрегации результатов каждой итераций цикла
 * @param selectorExpr условие (предикат), определяющее объекты, перебираемые в цикле ([BooleanType]);
 * проверяемый объект подставляется в предикат как контекстная переменная с именем и типом соответствующими переменной [variable]
 * @param variable переменная дерева решений, соответствующая текущему рассматриваемому объекту внутри тела цикла
 * @param thoughtBranch тело цикла: ветвь мысли, выполняемая для каждого перебираемого объекта
 */
class CycleAggregationNode(
    override val aggregationMethod: AggregationMethod,
    val selectorExpr: Operator,
    val variable: TypedVariable,
    val errorCategories: List<FindErrorCategory>,
    val thoughtBranch: ThoughtBranch,
    override val outcomes: Outcomes<BranchResult>,
) : AggregationNode() {
    init {
        errorCategories.forEach { it.initCheckedVariable(variable.className) }
    }

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(thoughtBranch).plus(outcomes)

    override fun validate(
        domainModel: DomainModel,
        results: DecisionTreeValidationResults,
        context: DecisionTreeContext
    ) {
        variable.checkValid(domainModel, results, context, this)

        val selectorType = selectorExpr.validateForDecisionTree(
            domainModel,
            results,
            context,
            withVariables = mapOf(variable.varName to variable.className)
        )
        results.checkValid(
            selectorType is BooleanType,
            "Selector expression for the $description returns $selectorType, but must return a boolean " +
                    "(be a predicate with respect to variable '${variable.varName}')"
        )

        validateLinked(domainModel, results, context, errorCategories)

        context.add(variable)
        thoughtBranch.validate(domainModel, results, context)
        context.remove(variable)
        validateLinked(domainModel, results, context, linkedElements.minus(thoughtBranch))
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}