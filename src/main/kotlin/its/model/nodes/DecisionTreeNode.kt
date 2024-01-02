package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour

/**
 * Узел дерева решений
 */
sealed class DecisionTreeNode : DecisionTreeElement() {

    /**
     * Применяет поведение [behaviour] к данному узлу
     * @param behaviour применяемое поведение
     * @return информация, возвращаемая поведением при обработке данного узла
     * @see DecisionTreeBehaviour
     */
    abstract fun <I> use(behaviour: DecisionTreeBehaviour<I>): I
}