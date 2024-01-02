package its.model.nodes

import its.model.TypedVariable
import its.model.definition.Domain
import its.model.definition.types.BooleanType
import its.model.definition.types.ObjectType
import its.model.expressions.Operator
import its.model.isPresent
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
        get() = listOf(varAssignment).plus(errorCategories).plus(secondaryAssignments).plus(outcomes.values)

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

        override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
            super.validate(domain, results, context)
            val selectorType = selectorExpr.validateForDecisionTree(
                domain,
                results,
                context,
                withVariables = mapOf(CHECKED_OBJ to parentNode.varAssignment.variable.className)
            )
            results.checkValid(
                selectorType is BooleanType,
                "Selector expression in a $description returns $selectorType, but must return a boolean " +
                        "(be a predicate with respect to variable '$CHECKED_OBJ')"
            )
        }
    }

    /**
     * Присвоение переменной в дереве решений
     *
     * Создает или заменяет переменную дерева решений [variable] со значением (объектом), вычисляемым по [valueExpr]
     *
     * @param variable определяемая переменная дерева решений
     * @param valueExpr выражение, вычисляющее значение переменной ([ObjectType])
     */
    class DecisionTreeVarAssignment(
        val variable: TypedVariable,
        val valueExpr: Operator,
    ) : HelperDecisionTreeElement() {
        override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
            super.validate(domain, results, context)
            variable.checkValid(domain, results, context, this)
            val valueType = valueExpr.validateForDecisionTree(domain, results, context)
            results.checkValid(
                valueType is ObjectType && ObjectType(variable.className).castFits(valueType, domain),
                "Value expression in $description ($parent) returns $valueType, but must return " +
                        "an object of type '${variable.className}' to conform to a variable ${variable.varName}"
            )
        }
    }

    override fun validate(domain: Domain, results: DecisionTreeValidationResults, context: DecisionTreeContext) {
        //Сначала валидируются части, в которых находимая переменная неизвестна
        validateLinked(domain, results, context,
            mutableListOf<DecisionTreeElement>(varAssignment).also {
                if (nextIfNone.isPresent) it.add(nextIfNone!!)
            }
        )

        //Далее части валидируются с добавлением в контекст известных переменных
        context.add(varAssignment.variable)
        validateLinked(domain, results, context, errorCategories)
        validateLinked(domain, results, context, secondaryAssignments)
        for ((i, secondaryAssignment) in secondaryAssignments.withIndex()) {
            val others = secondaryAssignments.subList(i + 1, secondaryAssignments.size)
            results.checkValid(
                others.none { it.variable.varName == secondaryAssignment.variable.varName },
                "Cannot declare multiple secondary assignments " +
                        "with the same name ${secondaryAssignment.variable.varName} (in $description)"
            )
        }
        secondaryAssignments.forEach { context.add(it.variable) }
        nextIfFound.validate(domain, results, context)
        context.remove(varAssignment.variable)
        secondaryAssignments.forEach { context.remove(it.variable) }
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}