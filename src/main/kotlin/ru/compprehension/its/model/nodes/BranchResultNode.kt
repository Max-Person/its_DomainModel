package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.ParseValue.parseValueForBranchResult
import ru.compprehension.its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class BranchResultNode(
    val value: Any,
    val actionExpr: Operator? = null,
) : DecisionTreeNode() {
    internal constructor(el: Element) : this(
        el.getAttribute("value").parseValueForBranchResult(),
        if (el.getChild("Expression") != null) Operator.build(el.getSingleByWrapper("Expression")!!) else null
    ) {
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}