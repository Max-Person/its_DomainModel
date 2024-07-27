package its.model.nodes

import its.model.definition.Domain
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

    init {
        errorCategories.forEach { it.initCheckedVariable(varAssignment.variable.className) }
    }

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(varAssignment).plus(errorCategories).plus(secondaryAssignments).plus(outcomes)

    val nextIfFound
        get() = outcomes[true]!!
    val nextIfNone
        get() = outcomes[false]

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