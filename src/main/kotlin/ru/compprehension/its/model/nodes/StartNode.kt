package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.nodes.visitors.DecisionTreeBehaviour
import ru.compprehension.its.model.nullCheck
import org.w3c.dom.Element

class StartNode(
    val initVariables: Map<String, String> = HashMap(),
    val main: ThoughtBranch
) : DecisionTreeNode() {
    internal constructor(el: Element) : this(
        el.getSeveralByWrapper("InputVariables").associate { it.getAttribute("name") to it.getAttribute("type") },
        ThoughtBranch(el.getChild("ThoughtBranch").nullCheck("StartNode has to have an associated ThoughtBranch")),
    ) {
        collectAdditionalInfo(el)
    }

    fun declaredVariables(): Map<String, String> {
        return initVariables
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}