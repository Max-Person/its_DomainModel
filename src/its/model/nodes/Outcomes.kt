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

class Outcomes<KeyType>(info: Collection<Outcome<KeyType>>) : InfoMap<Outcome<KeyType>, KeyType, DecisionTreeNode>(info.toSet()){
    internal constructor(el : Element, keyFromString : (str :String) -> KeyType)
            : this(
        el.getChildren("Outcome").map { Outcome(it, keyFromString) }
    )

    fun additionalInfo(key: KeyType) : Map<String, String>?{
        return getFull(key)?.additionalInfo
    }

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

class PredeterminingOutcomes(info: Collection<PredeterminingOutcome>) : InfoMap<PredeterminingOutcome, String, DecisionTreeNode>(info.toSet()){
    internal constructor(el : Element)
            : this(
        el.getChildren("Outcome").map { PredeterminingOutcome(it) }
    )

    fun additionalInfo(key: String) : Map<String, String>?{
        return getFull(key)?.additionalInfo
    }

    fun decidingBranch(key: String) : ThoughtBranch?{
        return getFull(key)?.decidingBranch
    }
}
