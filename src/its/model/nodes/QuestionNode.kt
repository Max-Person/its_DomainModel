package its.model.nodes

import its.model.expressions.Operator
import its.model.expressions.types.ParseValue.parseValue
import its.model.expressions.types.Types.typeFromString
import its.model.nodes.visitors.LinkNodeBehaviour
import org.w3c.dom.Element
import kotlin.reflect.KClass

class QuestionNode (
    val type: KClass<*>,
    val isSwitch : Boolean = false,
    val expr: Operator,
    override val next: Outcomes<Any>
): LinkNode<Any>() {
    internal constructor(el : Element) : this(
        typeFromString(el.getAttribute("type"))!!,
        el.getAttribute("isSwitch").toBoolean(),
        Operator.build(el.getSingleByWrapper("Expression")!!),
        getOutcomes(el) { it.parseValue(typeFromString(el.getAttribute("type"))!!)}
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}