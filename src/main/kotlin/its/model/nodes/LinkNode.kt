package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Общий класс для узлов-связок, имеющих переходы [outcomes] к дочерним узлам;
 *
 * Данный тип узлов составляет основную часть дерева решений
 */
sealed class LinkNode<AnswerType> : DecisionTreeNode() {
    /**
     * Переходы из текущего узла
     */
    abstract val outcomes: Outcomes<AnswerType>

    /**
     * Дочерние узлы для текущего узла
     */
    val children
        get() = outcomes.map { it.node }

    abstract fun <I> use(behaviour: LinkNodeBehaviour<I>): I
    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return this.use(behaviour as LinkNodeBehaviour<I>)
    }
}