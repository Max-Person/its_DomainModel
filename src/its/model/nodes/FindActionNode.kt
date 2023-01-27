package its.model.nodes

import its.model.expressions.Operator
import org.w3c.dom.Element


//FindAction пока выделен отдельно, но в случае появления новых действий можно выделить общий родительский класс
class FindActionNode(
    val selectorExpr: Operator,
    private val varName: String,
    private val varClass: String,
    val nextIfFound: DecisionTreeNode,
    val nextIfNone: DecisionTreeNode? = null,
) : DecisionTreeNode(), DecisionTreeVarDeclaration {
    internal constructor(el : Element) : this(
        Operator.build(el.getSingleByWrapper("Expression")),
        el.getChild("DecisionTreeVarDecl").getAttribute("name"),
        el.getChild("DecisionTreeVarDecl").getAttribute("type"),
        build(el.getByOutcome("found"))!!,
        build(el.getByOutcome("none")),
    )

    override fun declaredVariables(): Map<String, String> {
        val decl: MutableMap<String, String> = HashMap()
        decl[varName] = varClass
        return decl
    }
}