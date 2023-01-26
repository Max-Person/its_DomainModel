package its.model.nodes

import its.model.expressions.Literal
import org.w3c.dom.Element

class BranchResultNode(
    val value: Literal
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(Literal.fromString(el.getAttribute("value")))
}