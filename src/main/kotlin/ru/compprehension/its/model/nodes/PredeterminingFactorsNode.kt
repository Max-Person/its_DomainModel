package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.nodes.visitors.LinkNodeBehaviour
import org.w3c.dom.Element

class PredeterminingFactorsNode(
    override val next: PredeterminingOutcomes
) : LinkNode<String>() {
    val predetermining
        get() = next.info.filter { it.decidingBranch != null }
    val undetermined
        get() = next.info.singleOrNull { it.decidingBranch == null }

    internal constructor(el: Element) : this(
        getPredeterminingOutcomes(el)
    ) {
        require(next.info.filter { it.decidingBranch == null }.size <= 1) { "У узла предрешающих факторов может быть только один неопределенный переход" }
        collectAdditionalInfo(el)
    }

    override fun <I> use(behaviour: LinkNodeBehaviour<I>): I {
        return behaviour.process(this)
    }
}