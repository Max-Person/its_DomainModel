package its.model.nodes

import its.model.visitors.DecisionTreeSource
import its.model.visitors.DecisionTreeVisitor
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
        val info = mutableMapOf(DecisionTreeSource.fromCurrent(this) to visitor.process(this))
        info.putAll(predetermining.map { DecisionTreeSource.fromPredetermining(it) to it.accept(visitor) })
        info[DecisionTreeSource.fromOutcome("undetermined", undetermined)] = undetermined.accept(visitor)
        return visitor.process(this,  info)
    }
}