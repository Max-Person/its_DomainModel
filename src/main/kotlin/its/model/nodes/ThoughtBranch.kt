package its.model.nodes

/**
 * Ветвь мысли в дереве решений
 *
 * Ветви мысли соответствуют отдельным под-задачам (под-проблемам) в дереве решений,
 * и имеют собственные ответы (результаты, определяемые [BranchResultNode]),
 * которые влияют на более высокоуровневые рассуждения
 */
class ThoughtBranch(
    val start: DecisionTreeNode,
) : DecisionTreeElement() {

    override val linkedElements: List<DecisionTreeElement>
        get() = listOf(start)

//    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
//        return behaviour.process(this)
//    }
}