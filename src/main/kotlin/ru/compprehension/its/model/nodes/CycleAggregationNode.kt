package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.models.DecisionTreeVarModel
import ru.compprehension.its.model.nodes.visitors.LinkNodeBehaviour
import ru.compprehension.its.model.nullCheck
import org.w3c.dom.Element

class CycleAggregationNode(
    val logicalOp: LogicalOp,
    val selectorExpr: Operator,
    varName: String,
    varClass: String,
    val thoughtBranch: ThoughtBranch,
    override val next: Outcomes<Boolean>,
) : LinkNode<Boolean>() {
    val variable: DecisionTreeVarModel

    init {
        require(next.keys == setOf(true, false)) { "CycleAggregationNode has to have both true and false outcomes" }
        variable = FindActionNode.checkVar(varName, varClass)
    }

    internal constructor(el: Element) : this(
        LogicalOp.fromString(el.getAttribute("operator"))
            .nullCheck("CycleAggregationNode has to have a valid 'operator' attribute"),
        Operator.build(
            el.getSingleByWrapper("SelectorExpression")
                .nullCheck("CycleAggregationNode has to have a 'SelectorExpression' child tag")
        ),
        el.getChild("DecisionTreeVarDecl")
            .nullCheck("CycleAggregationNode has to have a 'DecisionTreeVarDecl' child tag").getAttribute("name"),
        el.getChild("DecisionTreeVarDecl")
            .nullCheck("CycleAggregationNode has to have a 'DecisionTreeVarDecl' child tag").getAttribute("type"),
        ThoughtBranch(
            el.getChild("ThoughtBranch").nullCheck("CycleAggregationNode has to have a 'ThoughtBranch' child tag")
        ),
        getOutcomes(el) { it.toBoolean() }
    ) {
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}