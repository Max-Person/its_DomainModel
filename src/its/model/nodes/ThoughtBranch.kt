package its.model.nodes

import its.model.expressions.types.DataType
import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
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
    )

    val isParametrized: Boolean
        get() = parameterName != null

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mapOf(
            InfoSource.fromCurrent(this) to visitor.process(this),
            InfoSource.fromOutcome("start", start) to start.accept(visitor),
        )
        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}