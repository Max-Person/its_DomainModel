package its.model.nodes

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import org.w3c.dom.Element

class QuestionNode (
    val type: DataType,
    val isSwitch : Boolean = false,
    val expr: Operator,
    val next: Map<Literal, DecisionTreeNode>,
): DecisionTreeNode() {
    internal constructor(el : Element) : this(
        DataType.valueOf(el.getAttribute("type"))!!,
        el.getAttribute("isSwitch").toBoolean(),
        Operator.build(el.getSingleByWrapper("Expression")),
        el.getChildren("Outcome").map { Literal.fromString(it.getAttribute("value")) to build(it.getChild())!!}.toMap()
    )
}