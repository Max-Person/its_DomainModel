package its.model.nodes

import its.model.expressions.types.DataType
import org.w3c.dom.Element

class ThoughtBranch (
    val type: DataType,
    val parameterName: String? = null,
    val start: DecisionTreeNode,
){
    internal constructor(el : Element) : this(
        DataType.fromString(el.getAttribute("type"))!!,
        el.getAttribute("paramName").ifBlank { null },
        DecisionTreeNode.build(el.getChild())!!,
    )

    val isParametrized: Boolean
        get() = parameterName != null
}