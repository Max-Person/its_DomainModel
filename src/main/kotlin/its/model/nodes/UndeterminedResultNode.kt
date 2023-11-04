package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour

class UndeterminedResultNode : DecisionTreeNode() {
    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}