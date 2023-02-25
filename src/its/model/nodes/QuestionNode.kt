package its.model.nodes

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class QuestionNode (
    val type: DataType,
    val enumOwner: String? = null,
    val isSwitch : Boolean = false,
    val expr: Operator,
    override val next: Outcomes<Literal>
): LinkNode<Literal>() {
    internal constructor(el : Element) : this(
        DataType.fromString(el.getAttribute("type"))!!,
        el.getAttribute("enumOwner").ifBlank { null },
        el.getAttribute("isSwitch").toBoolean(),
        Operator.build(el.getSingleByWrapper("Expression")!!),
        getOutcomes(el) { Literal.fromString(
            it,
            DataType.fromString(el.getAttribute("type"))!!,
            el.getAttribute("enumOwner").ifBlank { null }
        ) }
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}