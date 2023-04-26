package its.model.nodes

import its.model.nodes.visitors.LinkNodeBehaviour
import org.w3c.dom.Element

class LogicAggregationNode (
    val logicalOp: LogicalOp,
    val thoughtBranches: List<ThoughtBranch>,
    override val next: Outcomes<Boolean>,
) : LinkNode<Boolean>(){
    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        el.getChildren("ThoughtBranch").map { ThoughtBranch(it) },
        getOutcomes(el) { it.toBoolean() }
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}