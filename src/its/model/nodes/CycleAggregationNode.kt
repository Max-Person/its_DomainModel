package its.model.nodes

import its.model.expressions.Operator
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
        LogicalOp.valueOf(el.getAttribute("operator")),
        Operator.build(el.getSingleByWrapper("SelectorExpression")),
        el.getChild("DecisionTreeVarDecl").getAttribute("name"),
        el.getChild("DecisionTreeVarDecl").getAttribute("type"),
        ThoughtBranch(el.getChild("ThoughtBranch")),
        hashMapOf(
            true to build(el.getChildren("Outcome").first { it.getAttribute("value").equals("true")})!!,
            false to build(el.getChildren("Outcome").first { it.getAttribute("value").equals("false")})!!)
    )
}