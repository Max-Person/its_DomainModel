package its.model.nodes

import its.model.visitors.DecisionTreeSource
import its.model.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class LogicAggregationNode (
    val logicalOp: LogicalOp,
    val thoughtBranches: List<ThoughtBranch>,
    val next: Map<Boolean, DecisionTreeNode>,
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        el.getChildren("ThoughtBranch").map { ThoughtBranch(it) },
        hashMapOf(
            true to build(el.getByOutcome("true"))!!,
            false to build(el.getByOutcome("false"))!!)
    )

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mutableMapOf(DecisionTreeSource.fromCurrent(this) to visitor.process(this))
        info.putAll(thoughtBranches.map { DecisionTreeSource.fromBranch(it) to it.accept(visitor) })
        info[DecisionTreeSource.fromOutcome(true, next[true]!!)] = next[true]!!.accept(visitor)
        info[DecisionTreeSource.fromOutcome(false, next[false]!!)] = next[false]!!.accept(visitor)
        return visitor.process(this,  info)
    }
}