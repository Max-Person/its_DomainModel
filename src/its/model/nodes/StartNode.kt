package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class StartNode(
    val initVariables: Map<String, String> = HashMap(),
    val main: ThoughtBranch
) : DecisionTreeNode() {
    internal constructor(el : Element) : this(
        el.getSeveralByWrapper("InputVariables").associate { it.getAttribute("name") to it.getAttribute("type") },
        ThoughtBranch(el.getChild("ThoughtBranch")!!),
    ){
        collectAdditionalInfo(el)
    }

    fun declaredVariables(): Map<String, String> {
        return initVariables
    }

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mapOf(
            InfoSource.fromCurrent(this) to visitor.process(this),
            InfoSource.fromBranch(main) to main.accept(visitor),
        )
        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}