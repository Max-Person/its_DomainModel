package its.model.nodes

import its.model.expressions.Operator
import its.model.expressions.types.ParseValue.parseValue
import its.model.expressions.types.Types.typeFromString
import its.model.nodes.visitors.LinkNodeBehaviour
import its.model.nullCheck
import org.w3c.dom.Element
import kotlin.reflect.KClass

class QuestionNode(
    val type: KClass<*>,
    val isSwitch: Boolean = false,
    val expr: Operator,
    override val next: Outcomes<Any>,
    val trivialityExpr: Operator? = null,
) : LinkNode<Any>() {
    internal constructor(el: Element) : this(
        typeFromString(el.getAttribute("type")).nullCheck("QuestionNode has to have a valid 'type' attribute"),
        el.getAttribute("isSwitch").toBoolean(),
        Operator.build(
            el.getSingleByWrapper("Expression").nullCheck("QuestionNode has to have an 'Expression' child tag")
        ),
        getOutcomes(el) { it.parseValue(typeFromString(el.getAttribute("type")).nullCheck("QuestionNode's outcomes' values should match its type")) },
        el.getChild("Triviality")
            ?.let { Operator.build(it.getChild().nullCheck("Triviality must contain a valid expression XML")) },
    ) {
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }

    fun canBeTrivial(): Boolean = isSwitch || trivialityExpr != null
}