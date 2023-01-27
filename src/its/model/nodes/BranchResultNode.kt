package its.model.nodes

import its.model.expressions.Literal
import its.model.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class BranchResultNode(
    val value: Literal
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(Literal.fromString(el.getAttribute("value")))

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        return visitor.process(this)
    }
}