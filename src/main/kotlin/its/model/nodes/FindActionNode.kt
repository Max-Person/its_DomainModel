package its.model.nodes

import its.model.DomainModel
import its.model.expressions.Operator
import its.model.models.DecisionTreeVarModel
import its.model.nodes.visitors.LinkNodeBehaviour
import its.model.nullCheck
import org.w3c.dom.Element


//FindAction пока выделен отдельно, но в случае появления новых действий можно выделить общий родительский класс
class FindActionNode(
    val selectorExpr: Operator,
    varName: String,
    varClass: String,
    val errorCategories: List<FindErrorCategory>,
    val additionalVariables: List<AdditionalVarDeclaration>,
    override val next: Outcomes<String>,
) : LinkNode<String>(), DecisionTreeVarDeclaration {
    val variable: DecisionTreeVarModel
    val nextIfFound
        get() = next["found"]!!
    val nextIfNone
        get() = next["none"]

    class FindErrorCategory(
        val priority: Int,
        val selectorExpr: Operator,
        val additionalInfo: Map<String, String> = mapOf()
    ) {
        constructor(el: Element, className: String) : this(
            el.getAttribute("priority").toIntOrNull()
                .nullCheck("Find Error Category has to have a valid int 'prioroty' attribute"),
            Operator.build(
                el.getSingleByWrapper("Expression")
                    .nullCheck("Find Error Category has to have an 'Expression' child tag"),
                mapOf("checked" to className),
            ),
            el.getAdditionalInfo()
        )

        override fun toString(): String {
            return this.additionalInfo["alias"] ?: this.additionalInfo["label"] ?: super.toString()
        }
    }

    class AdditionalVarDeclaration(
        varName: String,
        varClass: String,
        val calcExpr: Operator,
    ) {
        val variable: DecisionTreeVarModel

        init {
            variable = checkVar(varName, varClass)
        }

        constructor(el: Element) : this(
            el.getAttribute("name"),
            el.getAttribute("type"),
            Operator.build(el.getSingleByWrapper("Expression")!!),
        )
    }

    init {
        next.keys.forEach { require(it == "none" || it == "found") { "FindActionNode cannot have an outcome with a value $it" } }
        variable = checkVar(varName, varClass)
    }

    companion object _static {
        @JvmStatic
        fun checkVar(varName: String, varClass: String): DecisionTreeVarModel {
            require(DomainModel.decisionTreeVarsDictionary.contains(varName)) {
                "Переменная $varName, используемая в дереве решений, не объявлена в словаре"
            }
            require(DomainModel.decisionTreeVarsDictionary.getClass(varName) == varClass) {
                "Переменная $varName, используемая в дереве решений, объявлена с классом, не совпадающим с объявлением в словаре"
            }
            return DomainModel.decisionTreeVarsDictionary.get(varName)!!
        }
    }

    internal constructor(el: Element) : this(
        Operator.build(el.getSingleByWrapper("Expression")!!),
        el.getChild("DecisionTreeVarDecl").nullCheck("FindActionNode has to have a 'DecisionTreeVarDecl' child tag")
            .getAttribute("name"),
        el.getChild("DecisionTreeVarDecl").nullCheck("FindActionNode has to have a 'DecisionTreeVarDecl' child tag")
            .getAttribute("type"),
        el.getChildren("FindError")
            .map { errEl -> FindErrorCategory(errEl, el.getChild("DecisionTreeVarDecl")!!.getAttribute("type")) }
            .sortedBy { category -> category.priority },
        el.getChildren("AdditionalVarDecl").map { declEl -> AdditionalVarDeclaration(declEl) },
        getOutcomes(el) { it }
    ) {
        collectAdditionalInfo(el)
    }

    override fun declaredVariable(): DecisionTreeVarModel {
        return variable
    }

    override fun declarationExpression(): Operator {
        return selectorExpr
    }

    fun allDeclaredVariables(): Set<String> {
        return setOf(this.variable.name).plus(this.additionalVariables.map { it.variable.name })
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}