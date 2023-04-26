package its.model.nodes

import its.model.nodes.visitors.LinkNodeBehaviour
import org.w3c.dom.Element

class PredeterminingFactorsNode (
    override val next: PredeterminingOutcomes
) : LinkNode<String>(){
    val predetermining
        get() = next.filterKeys { it.startsWith("predetermining") }
    val undetermined
        get() = next["undetermined"]

    internal constructor(el : Element) : this(
        getPredeterminingOutcomes(el)
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}