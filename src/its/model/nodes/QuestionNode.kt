package its.model.nodes

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class QuestionNode (
    val type: DataType,
    val enumOwner: String? = null,
    val isSwitch : Boolean = false,
    val expr: Operator,
    val next: Outcomes<Literal>
): DecisionTreeNode(), LinkNode {
    internal constructor(el : Element) : this(
        DataType.fromString(el.getAttribute("type"))!!,
        el.getAttribute("enumOwner").ifBlank { null },
        el.getAttribute("isSwitch").toBoolean(),
        Operator.build(el.getSingleByWrapper("Expression")!!),
        Outcomes(el) { Literal.fromString(
            it,
            DataType.fromString(el.getAttribute("type"))!!,
            el.getAttribute("enumOwner").ifBlank { null }
        ) }
    ){
        collectAdditionalInfo(el)
    }

    override val children: List<DecisionTreeNode>
        get() = next.values.toList()

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mutableMapOf(InfoSource.fromCurrent(this) to visitor.process(this))
        info.putAll(next.map { InfoSource.fromOutcome(it.key, it.value) to it.value.accept(visitor) })
        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}