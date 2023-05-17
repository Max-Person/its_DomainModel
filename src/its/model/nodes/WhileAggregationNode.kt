package its.model.nodes

import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour
import org.w3c.dom.Element

class WhileAggregationNode (
    val logicalOp: LogicalOp,
    val conditionExpr: Operator,
    val thoughtBranch: ThoughtBranch,
    override val next: Outcomes<Boolean>,
    val isWhile : Boolean = false
) : LinkNode<Boolean>(){

    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        Operator.build(el.getSingleByWrapper("SelectorExpression")!!),
        ThoughtBranch(el.getChild("ThoughtBranch")!!),
        getOutcomes(el) { it.toBoolean() },
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}