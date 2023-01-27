package its.model.visitors

import its.model.nodes.DecisionTreeNode
import its.model.nodes.ThoughtBranch

class DecisionTreeSource private constructor(val type : SourceType, val node : DecisionTreeNode, val outcomeValue : Any?) {
    enum class SourceType{
        fromOutcome,
        fromPredetermining,
        fromBranch,
        fromCurrent
    }

    companion object _static{
        @JvmStatic
        fun fromCurrent(current : DecisionTreeNode) : DecisionTreeSource{
            return DecisionTreeSource(SourceType.fromCurrent, current, null)
        }
        @JvmStatic
        fun fromOutcome(outcomeValue: Any, outcomeLink : DecisionTreeNode) : DecisionTreeSource{
            return DecisionTreeSource(SourceType.fromOutcome, outcomeLink, outcomeValue)
        }
        @JvmStatic
        fun fromPredetermining(predetermining : DecisionTreeNode) : DecisionTreeSource{
            return DecisionTreeSource(SourceType.fromPredetermining, predetermining, null)
        }
        @JvmStatic
        fun fromBranch(branch : ThoughtBranch) : DecisionTreeSource{
            return DecisionTreeSource(SourceType.fromBranch, branch, null)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DecisionTreeSource

        if (type != other.type) return false
        if (node != other.node) return false
        if (outcomeValue != other.outcomeValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + node.hashCode()
        result = 31 * result + (outcomeValue?.hashCode() ?: 0)
        return result
    }

}