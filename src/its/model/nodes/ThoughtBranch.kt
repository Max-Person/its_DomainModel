package its.model.nodes

import its.model.expressions.types.DataType
import its.model.visitors.DecisionTreeSource
import its.model.visitors.DecisionTreeVisitor
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
            DecisionTreeSource.fromCurrent(this) to visitor.process(this),
            DecisionTreeSource.fromOutcome("start", start) to start.accept(visitor),
        )
        return visitor.process(this,  info)
    }
}