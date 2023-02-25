package its.model.nodes

import its.model.expressions.Operator
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class CycleAggregationNode (
    val logicalOp: LogicalOp,
    val selectorExpr: Operator,
    private val varName: String,
    private val varClass: String,
    val thoughtBranch: ThoughtBranch,
    val next: Outcomes<Boolean>,
) : DecisionTreeNode(), LinkNode{
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

    override val children: List<DecisionTreeNode>
        get() = next.values.toList()

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}