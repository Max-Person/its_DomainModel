package its.model.nodes

import its.model.expressions.types.ParseValue.parseValueForBranchResult
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class BranchResultNode(
    val value: Any
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(el.getAttribute("value").parseValueForBranchResult()){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}