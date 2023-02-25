package its.model.nodes

import its.model.Info
import its.model.InfoMap
import org.w3c.dom.Element

open class Outcome<KeyType>(
    override val key: KeyType,
    override val value: DecisionTreeNode,
    val additionalInfo : Map<String, String> = emptyMap()
) : Info<KeyType, DecisionTreeNode>{
    internal constructor(el : Element, keyFromString : (str :String) -> KeyType)
            : this(
        keyFromString(el.getAttribute("value")),
        DecisionTreeNode.build(el.getChildren().first{DecisionTreeNode.canBuildFrom(it)})!!,
        el.getAdditionalInfo(),
    )

}

typealias Outcomes<KeyType> = InfoMap<out Outcome<KeyType>, KeyType, DecisionTreeNode>

internal fun <KeyType> getOutcomes(el : Element, keyFromString : (str :String) -> KeyType) : Outcomes<KeyType>{
    return Outcomes(el.getChildren("Outcome").map { Outcome(it, keyFromString) }.toSet())
}

fun <KeyType> Outcomes<KeyType>.additionalInfo(key: KeyType) : Map<String, String>?{
    return this.getFull(key)?.additionalInfo
}

class PredeterminingOutcome(
    key: String,
    value: DecisionTreeNode,
    additionalInfo : Map<String, String> = emptyMap(),
    val decidingBranch: ThoughtBranch?,
) : Outcome<String>(key, value, additionalInfo) {
    internal constructor(el : Element)
            : this(
        el.getAttribute("value"),
        DecisionTreeNode.build(el.getChildren().first { DecisionTreeNode.canBuildFrom(it) })!!,
        el.getAdditionalInfo(),
        if(el.getChild("ThoughtBranch") != null) ThoughtBranch(el.getChild("ThoughtBranch")!!) else null,
    )
}

typealias PredeterminingOutcomes = InfoMap<PredeterminingOutcome, String, DecisionTreeNode>

internal fun getPredeterminingOutcomes(el : Element) : PredeterminingOutcomes{
    return PredeterminingOutcomes(el.getChildren("Outcome").map { PredeterminingOutcome(it) }.toSet())
}

fun PredeterminingOutcomes.predeterminingBranch(key: String) : ThoughtBranch?{
    return this.getFull(key)?.decidingBranch
}
