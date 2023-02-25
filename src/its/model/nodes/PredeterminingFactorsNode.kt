package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import org.w3c.dom.Element

class PredeterminingFactorsNode (
    val next: PredeterminingOutcomes
) : DecisionTreeNode(), LinkNode{
    val predetermining
        get() = next.filterKeys { it.startsWith("predetermining") }
    val undetermined
        get() = next["undetermined"]

    internal constructor(el : Element) : this(
        PredeterminingOutcomes(el)
    ){
        collectAdditionalInfo(el)
    }

    override val children: List<DecisionTreeNode>
        get() = next.values.toList()

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}