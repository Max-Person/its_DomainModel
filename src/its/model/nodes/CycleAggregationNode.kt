package its.model.nodes

import its.model.expressions.Operator
import its.model.visitors.DecisionTreeSource
import its.model.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class CycleAggregationNode (
    val logicalOp: LogicalOp,
    val selectorExpr: Operator,
    private val varName: String,
    private val varClass: String,
    val thoughtBranch: ThoughtBranch,
    val next: Map<Boolean, DecisionTreeNode>,
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        Operator.build(el.getSingleByWrapper("SelectorExpression")),
        el.getChild("DecisionTreeVarDecl").getAttribute("name"),
        el.getChild("DecisionTreeVarDecl").getAttribute("type"),
        ThoughtBranch(el.getChild("ThoughtBranch")),
        hashMapOf(
            true to build(el.getByOutcome("true"))!!,
            false to build(el.getByOutcome("false"))!!)
    )

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mapOf(
            DecisionTreeSource.fromCurrent(this) to visitor.process(this),
            DecisionTreeSource.fromBranch(thoughtBranch) to thoughtBranch.accept(visitor),
            DecisionTreeSource.fromOutcome(true, next[true]!!) to next[true]!!.accept(visitor),
            DecisionTreeSource.fromOutcome(false, next[false]!!) to next[false]!!.accept(visitor),
        )
        return visitor.process(this,  info)
    }
}