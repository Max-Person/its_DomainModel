package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor.InfoSource
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element

class PredeterminingFactorsNode (
    val next: PredeterminingOutcomes
) : DecisionTreeNode(){
    val predetermining
        get() = next.filterKeys { it.startsWith("predetermining") }
    val undetermined
        get() = next["undetermined"]

    internal constructor(el : Element) : this(
        PredeterminingOutcomes(el)
    ){
        collectAdditionalInfo(el)
    }

    override fun <I> accept(visitor: DecisionTreeVisitor<I>): I {
        val info = mutableMapOf(InfoSource.fromCurrent(this) to visitor.process(this))
        info.putAll(predetermining.values.map { InfoSource.fromPredetermining(it) to it.accept(visitor) })
        if(undetermined != null){
            info[InfoSource.fromOutcome("undetermined", undetermined!!)] = undetermined!!.accept(visitor)
        }
        return visitor.process(this,  info)
    }

    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return behaviour.process(this)
    }
}