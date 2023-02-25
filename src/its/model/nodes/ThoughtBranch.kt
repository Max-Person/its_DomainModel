package its.model.nodes

import its.model.expressions.types.DataType
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class ThoughtBranch(
    val type: DataType,
    val parameterName: String? = null,
    val start: DecisionTreeNode,
) : DecisionTreeNode() {
    internal constructor(el : Element) : this(
        DataType.fromString(el.getAttribute("type"))!!,
        el.getAttribute("paramName").ifBlank { null },
        build(el.getChild())!!,
    ){
        collectAdditionalInfo(el)
    }

    val isParametrized: Boolean
        get() = parameterName != null

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}