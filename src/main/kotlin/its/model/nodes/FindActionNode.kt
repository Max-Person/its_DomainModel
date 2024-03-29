package its.model.nodes

import its.model.TypedVariable
import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Узел действия (поиска объекта)
 *
 * Вычисляет и присваивает значение переменной дерева мысли согласно [varAssignment] (находит объект и запоминает его).
 * Если объект найден, то выполняет также дополнительные присвоения [secondaryAssignments] и переходит по выходу `true`
 * Иначе переходит по выходу `false`
 *
 * @param varAssignment основное присвоение переменной в узле
 * @param errorCategories список категорий возможных в данном узле ошибок
 * @param secondaryAssignments дополнительные присвоения переменных, если основное было выполнено
 * (данные присвоения могут ссылаться на переменную дерева мысли, определяемую в [varAssignment], т.к. выполняются только если оно выполнено успешно)
 */
//FindAction пока выделен отдельно, но в случае появления новых действий можно выделить общий родительский класс
class FindActionNode(
    val varAssignment: DecisionTreeVarAssignment,
    val errorCategories: List<FindErrorCategory>,
    val secondaryAssignments: List<DecisionTreeVarAssignment>,
    override val outcomes: Outcomes<Boolean>,
) : LinkNode<Boolean>() {
    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(varAssignment).plus(errorCategories).plus(secondaryAssignments).plus(outcomes)

    val nextIfFound
        get() = outcomes[true]!!
    val nextIfNone
        get() = outcomes[false]

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
    ) : HelperDecisionTreeElement() {
        private val parentNode
            get() = (parent as FindActionNode)

        companion object {
            const val CHECKED_OBJ = "checked"
        }

        val checkedVariable
            get() = TypedVariable(parentNode.varAssignment.variable.className, CHECKED_OBJ)

        override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
            super.validate(domain, results, context)
            val selectorType = selectorExpr.validateForDecisionTree(
                domain,
                results,
                context,
                withVariables = mapOf(checkedVariable.varName to checkedVariable.className)
            )
            results.checkValid(
                selectorType is BooleanType,
                "Selector expression in a $description returns $selectorType, but must return a boolean " +
                        "(be a predicate with respect to variable '$CHECKED_OBJ')"
            )
        }
    }

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        //Сначала валидируются части, в которых находимая переменная неизвестна
        validateLinked(domain, results, context,
            mutableListOf<DecisionTreeElement>(varAssignment).also {
                if (nextIfNone != null) it.add(nextIfNone!!)
            }
        )

        //Далее части валидируются с добавлением в контекст известных переменных
        context.add(varAssignment.variable)
        validateLinked(domain, results, context, errorCategories)
        secondaryAssignments.validate(domain, results, context, this)
        nextIfFound.validate(domain, results, context)
        context.remove(varAssignment.variable)
        secondaryAssignments.forEach { context.remove(it.variable) }
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}