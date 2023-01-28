package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class PredeterminingFactorsNode (
    val predetermining: List<DecisionTreeNode> = emptyList(),
    val undetermined: DecisionTreeNode,
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(
        el.getSeveralByWrapper("Predetermining").map { build(it)!!},
        build(el.getByOutcome("undetermined"))!!
    )

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mutableMapOf(InfoSource.fromCurrent(this) to visitor.process(this))
        info.putAll(predetermining.map { InfoSource.fromPredetermining(it) to it.accept(visitor) })
        info[InfoSource.fromOutcome("undetermined", undetermined)] = undetermined.accept(visitor)
        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}