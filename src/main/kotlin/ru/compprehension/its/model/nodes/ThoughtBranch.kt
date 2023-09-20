package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.expressions.types.Types.typeFromString
import ru.compprehension.its.model.nodes.visitors.DecisionTreeBehaviour
import ru.compprehension.its.model.nullCheck
import org.w3c.dom.Element
import kotlin.reflect.KClass

class ThoughtBranch(
    val type: KClass<*>,
    val parameterName: String? = null,
    val start: DecisionTreeNode,
) : DecisionTreeNode() {
    internal constructor(el: Element) : this(
        typeFromString(el.getAttribute("type")).nullCheck("ThoughtBranch has to have a valid 'type' attribute"),
        el.getAttribute("paramName").ifBlank { null },
        build(el.getChild()).nullCheck("No nodes can be built from tag ${el.getChild()?.tagName}"),
    ) {
        collectAdditionalInfo(el)
    }

    val isParametrized: Boolean
        get() = parameterName != null

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}