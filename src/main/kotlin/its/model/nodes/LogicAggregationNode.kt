package its.model.nodes

import its.model.nodes.visitors.LinkNodeBehaviour
import its.model.nullCheck
import org.w3c.dom.Element

class LogicAggregationNode(
    val logicalOp: LogicalOp,
    val thoughtBranches: List<ThoughtBranch>,
    override val next: Outcomes<Boolean>,
) : LinkNode<Boolean>() {
    init {
        require(next.keys == setOf(true, false)) { "LogicAggregationNode has to have both true and false outcomes" }
        require(thoughtBranches.isNotEmpty()) { "LogicAggregationNode has to have at least one ThoughtBranch" }
    }

    internal constructor(el: Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))
            .nullCheck("LogicAggregationNode has to have a valid 'operator' attribute"),
        el.getChildren("ThoughtBranch").map { ThoughtBranch(it) },
        getOutcomes(el) { it.toBoolean() }
    ) {
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}