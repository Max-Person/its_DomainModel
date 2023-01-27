package its.model.nodes

import org.w3c.dom.Element

class PredeterminingFactorsNode (
    val predetermining: List<DecisionTreeNode> = emptyList(),
    val undetermined: DecisionTreeNode,
) : DecisionTreeNode(){
    internal constructor(el : Element) : this(
        el.getSeveralByWrapper("Predetermining").map { build(it)!!},
        build(el.getByOutcome("undetermined"))!!
    )
}