package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.LinkNodeBehaviour

/**
 * Общий класс для узлов-связок, имеющих переходы [outcomes] к дочерним узлам;
 *
 * Данный тип узлов составляет основную часть дерева решений
 */
sealed class LinkNode<AnswerType : Any> : DecisionTreeNode() {
    /**
     * Переходы из текущего узла
     */
    abstract val outcomes: Outcomes<AnswerType>

    /**
     * Дочерние узлы для текущего узла
     */
    val children
        get() = outcomes.map { it.node }

    /**
     * Получить узел, к которому будет совершен переход из текущего узла, в случае ответа [answer]
     */
    fun getNextNode(answer: AnswerType): DecisionTreeNode? {
        return this.outcomes[answer]?.node
    }

    abstract fun <I> use(behaviour: LinkNodeBehaviour<I>): I
    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return this.use(behaviour as LinkNodeBehaviour<I>)
    }
}