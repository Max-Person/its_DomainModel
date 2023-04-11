package its.model.nodes

import its.model.expressions.types.Types.typeFromString
import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element
import kotlin.reflect.KClass

class ThoughtBranch(
    val type: KClass<*>,
    val parameterName: String? = null,
    val start: DecisionTreeNode,
) : DecisionTreeNode() {
    internal constructor(el : Element) : this(
        typeFromString(el.getAttribute("type"))!!,
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