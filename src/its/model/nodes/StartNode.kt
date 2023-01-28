package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class StartNode(
    val initVariables: Map<String, String> = HashMap(),
    val main: ThoughtBranch
) : DecisionTreeNode(), DecisionTreeVarDeclaration {
    internal constructor(el : Element) : this(
        el.getChildren("DecisionTreeVarDecl").map{it.getAttribute("name") to it.getAttribute("type")}.toMap(),
        ThoughtBranch(el.getChild("ThoughtBranch")),
    ){
        collectAdditionalInfo(el)
    }

    override fun declaredVariables(): Map<String, String> {
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