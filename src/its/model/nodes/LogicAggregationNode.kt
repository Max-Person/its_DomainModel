package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class LogicAggregationNode (
    val logicalOp: LogicalOp,
    val thoughtBranches: List<ThoughtBranch>,
    val next: Outcomes<Boolean>,
) : DecisionTreeNode(), LinkNode{
    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        el.getChildren("ThoughtBranch").map { ThoughtBranch(it) },
        Outcomes(el) { it.toBoolean() }
    ){
        collectAdditionalInfo(el)
    }

    override val children: List<DecisionTreeNode>
        get() = next.values.toList()

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}