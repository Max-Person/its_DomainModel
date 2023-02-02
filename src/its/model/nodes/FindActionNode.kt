package its.model.nodes

import its.model.DomainModel
import its.model.expressions.Operator
import its.model.models.DecisionTreeVarModel
import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element


//FindAction пока выделен отдельно, но в случае появления новых действий можно выделить общий родительский класс
class FindActionNode(
    val selectorExpr: Operator,
    varName: String,
    varClass: String,
    val nextIfFound: DecisionTreeNode,
    val nextIfNone: DecisionTreeNode? = null,
) : DecisionTreeNode(), DecisionTreeVarDeclaration {
    val variable: DecisionTreeVarModel
    init {
        require(DomainModel.decisionTreeVarsDictionary.contains(varName)){
            "Переменная $varName, используемая в дереве решений, не объявлена в словаре"
        }
        require(DomainModel.decisionTreeVarsDictionary.getClass(varName) == varClass){
            "Переменная $varName, используемая в дереве решений, объявлена с классом, не совпадающим с объявлением в словаре"
        }
        variable = DomainModel.decisionTreeVarsDictionary.get(varName)!!
    }

    internal constructor(el : Element) : this(
        Operator.build(el.getSingleByWrapper("Expression")),
        el.getChild("DecisionTreeVarDecl").getAttribute("name"),
        el.getChild("DecisionTreeVarDecl").getAttribute("type"),
        build(el.getByOutcome("found"))!!,
        build(el.getByOutcome("none")),
    ){
        collectAdditionalInfo(el)
    }

    override fun declaredVariable(): DecisionTreeVarModel {
        return variable
    }

    override fun declarationExpression(): Operator {
        return selectorExpr
    }

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mutableMapOf(
            InfoSource.fromCurrent(this) to visitor.process(this),
            InfoSource.fromOutcome("found", nextIfFound) to nextIfFound.accept(visitor),
        )
        if(nextIfNone != null)
            info.put(InfoSource.fromOutcome("none", nextIfNone), nextIfNone.accept(visitor))

        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}