package its.model.nodes

import its.model.expressions.Operator
import its.model.nodes.visitors.LinkNodeBehaviour
import org.w3c.dom.Element

class CycleAggregationNode (
    val logicalOp: LogicalOp,
    val selectorExpr: Operator,
    private val varName: String,
    private val varClass: String,
    val thoughtBranch: ThoughtBranch,
    override val next: Outcomes<Boolean>,
) : LinkNode<Boolean>(){
    internal constructor(el : Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))!!,
        Operator.build(el.getSingleByWrapper("SelectorExpression")!!),
        el.getChild("DecisionTreeVarDecl")!!.getAttribute("name"),
        el.getChild("DecisionTreeVarDecl")!!.getAttribute("type"),
        ThoughtBranch(el.getChild("ThoughtBranch")!!),
        getOutcomes(el) { it.toBoolean() }
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}