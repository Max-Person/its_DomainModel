package its.model.nodes

import its.model.expressions.Literal
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class BranchResultNode(
    val value: Literal
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(Literal.fromString(el.getAttribute("value"))){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}