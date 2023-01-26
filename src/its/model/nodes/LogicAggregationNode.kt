package its.model.nodes

import org.w3c.dom.Element

class LogicAggregationNode (
    val logicalOp: LogicalOp,
    val thoughtBranches: List<ThoughtBranch>,
    val next: Map<Boolean, DecisionTreeNode>,
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(
        LogicalOp.valueOf(el.getAttribute("operator")),
        el.getChildren("ThoughtBranch").map { ThoughtBranch(it) },
        hashMapOf(
            true to build(el.getChildren("Outcome").first { it.getAttribute("value").equals("true")})!!,
            false to build(el.getChildren("Outcome").first { it.getAttribute("value").equals("false")})!!)
    )
}