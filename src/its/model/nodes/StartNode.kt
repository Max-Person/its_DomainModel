package its.model.nodes

import org.w3c.dom.Element

class StartNode(
    val initVariables: Map<String, String> = HashMap(),
    val main: ThoughtBranch
) : DecisionTreeNode(), DecisionTreeVarDeclaration {
    internal constructor(el : Element) : this(
        el.getChildren("DecisionTreeVarDecl").map{it.getAttribute("name") to it.getAttribute("type")}.toMap(),
        ThoughtBranch(el.getChild("ThoughtBranch")),
    )

    override fun declaredVariables(): Map<String, String> {
        return initVariables
    }
}