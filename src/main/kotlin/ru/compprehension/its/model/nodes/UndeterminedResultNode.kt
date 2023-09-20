package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.nodes.visitors.DecisionTreeBehaviour

class UndeterminedResultNode : DecisionTreeNode() {
    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}