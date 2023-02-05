package its.model.nodes

import its.model.expressions.Operator
import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class CycleAggregationNode (
    val logicalOp: LogicalOp,
    val selectorExpr: Operator,
    private val varName: String,
    private val varClass: String,
    val thoughtBranch: ThoughtBranch,
    val next: Outcomes<Boolean>,
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        Operator.build(el.getSingleByWrapper("SelectorExpression")!!),
        el.getChild("DecisionTreeVarDecl")!!.getAttribute("name"),
        el.getChild("DecisionTreeVarDecl")!!.getAttribute("type"),
        ThoughtBranch(el.getChild("ThoughtBranch")!!),
        Outcomes(el) { it.toBoolean() }
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mapOf(
            InfoSource.fromCurrent(this) to visitor.process(this),
            InfoSource.fromBranch(thoughtBranch) to thoughtBranch.accept(visitor),
            InfoSource.fromOutcome(true, next[true]!!) to next[true]!!.accept(visitor),
            InfoSource.fromOutcome(false, next[false]!!) to next[false]!!.accept(visitor),
        )
        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}