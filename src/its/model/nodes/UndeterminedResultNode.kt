package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor

class UndeterminedResultNode : DecisionTreeNode(){
    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
       return visitor.process(this)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}