package its.model.nodes

import its.model.TypedVariable
import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел циклической агрегации
 *
 * Выполняет ветку [thoughtBranch] для каждого объекта соответствующего условию [selectorExpr]
 * (внутри ветки к объекту можно обращаться с помощью переменной [variable])
 * и агрегирует результаты ветви по оператору [logicalOp];
 * Дальнейшие переходы осуществляются в зависимости от результата агрегации
 *
 * @param logicalOp логический оператор, агрегирующий результаты каждой итерации цикла
 * @param selectorExpr условие (предикат), определяющее объекты, перебираемые в цикле ([BooleanType]);
 * проверяемый объект подставляется в предикат как контекстная переменная с именем и типом соответствующими переменной [variable]
 * @param variable переменная дерева решений, соответствующая текущему рассматриваемому объекту внутри тела цикла
 * @param thoughtBranch тело цикла: ветвь мысли, выполняемая для каждого перебираемого объекта
 */
class CycleAggregationNode(
    val logicalOp: LogicalOp,
    val selectorExpr: Operator,
    val variable: TypedVariable,
    val thoughtBranch: ThoughtBranch,
    override val outcomes: Outcomes<Boolean>,
) : LinkNode<Boolean>() {
    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(thoughtBranch).plus(outcomes.values)

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        variable.checkValid(domain, results, context, this)

        val selectorType = selectorExpr.validateForDecisionTree(
            domain,
            results,
            context,
            withVariables = mapOf(variable.varName to variable.className)
        )
        results.checkValid(
            selectorType is BooleanType,
            "Selector expression for the $description returns $selectorType, but must return a boolean " +
                    "(be a predicate with respect to variable '${variable.varName}')"
        )
        results.checkValid(
            outcomes.containsKey(true) && outcomes.containsKey(false),
            "$description has to have both true and false outcomes"
        )

        context.add(variable)
        thoughtBranch.validate(domain, results, context)
        context.remove(variable)
        validateLinked(domain, results, context, linkedElements.minus(thoughtBranch))
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}