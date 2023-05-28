package its.model.nodes

import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour
import its.model.nullCheck
import org.w3c.dom.Element

class WhileAggregationNode (
    val logicalOp: LogicalOp,
    val conditionExpr: Operator,
    val thoughtBranch: ThoughtBranch,
    override val next: Outcomes<Boolean>,
    val isWhile : Boolean = false
) : LinkNode<Boolean>(){

    init {
        require(next.keys == setOf(true, false)){"WhileAggregationNode has to have both true and false outcomes"}
    }

    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator")).nullCheck("WhileAggregationNode has to have a valid 'operator' attribute"),
        Operator.build(el.getSingleByWrapper("SelectorExpression").nullCheck("WhileAggregationNode has to have a 'SelectorExpression' child tag")),
        ThoughtBranch(el.getChild("ThoughtBranch").nullCheck("WhileAggregationNode has to have a 'ThoughtBranch' child tag")),
        getOutcomes(el) { it.toBoolean() },
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}