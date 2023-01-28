package its.model.nodes.visitors

import its.model.nodes.*

/**
 * Интерфейс, описывающий некоторое поведение, применяемое при обходе узлов дерева решений (подклассы [DecisionTreeNode])
 *
 * Отличие данного интерфейса от [DecisionTreeBehaviour] в том,
 * что [DecisionTreeVisitor] реализовывается с целью полного обхода дерева решений
 * (Функция [DecisionTreeNode.accept] реализует логику обхода дерева).
 * Таким образом, данный класс стоит считать частным случаем соответсвующего -Behaviour класса.
 * @param Info тип возвращаемого функциями обработки значения
 * @see DecisionTreeNode.accept
 */
interface DecisionTreeVisitor<Info>{
    // ---------------------- Для узлов дерева решений ---------------------------
    // ---------------------- До обработки дочерних узлов ------------------------
    fun process(node: BranchResultNode) : Info
    fun process(node: CycleAggregationNode) : Info
    fun process(node: FindActionNode) : Info
    fun process(node: LogicAggregationNode) : Info
    fun process(node: PredeterminingFactorsNode) : Info
    fun process(node: QuestionNode) : Info
    fun process(node: StartNode) : Info
    fun process(branch: ThoughtBranch) : Info
    fun process(node: UndeterminedResultNode) : Info

    // ---------------------- Для узлов дерева решений ---------------------------
    // -------------------- После обработки дочерних узлов -------------------------
    fun process(node: CycleAggregationNode, info: Map<InfoSource, Info>) : Info
    fun process(node: FindActionNode,       info: Map<InfoSource, Info>) : Info
    fun process(node: LogicAggregationNode, info: Map<InfoSource, Info>) : Info
    fun process(node: PredeterminingFactorsNode, info: Map<InfoSource, Info>) : Info
    fun process(node: QuestionNode,         info: Map<InfoSource, Info>) : Info
    fun process(node: StartNode,            info: Map<InfoSource, Info>) : Info
    fun process(branch: ThoughtBranch,      info: Map<InfoSource, Info>) : Info

    /**
     * Источник возвращаемой инфморации для [DecisionTreeVisitor]
     * @param type тип источника
     * @param node узел, вернувший информацию
     * @param outcomeValue значение исхода, соответствующего данному источнику (для type = fromOutcome)
     */
    class InfoSource private constructor(val type : SourceType, val node : DecisionTreeNode, val outcomeValue : Any?) {
        enum class SourceType{
            fromOutcome,
            fromPredetermining,
            fromBranch,
            fromCurrent
        }

        companion object _static{
            @JvmStatic
            fun fromCurrent(current : DecisionTreeNode) : InfoSource {
                return InfoSource(SourceType.fromCurrent, current, null)
            }
            @JvmStatic
            fun fromOutcome(outcomeValue: Any, outcomeLink : DecisionTreeNode) : InfoSource {
                return InfoSource(SourceType.fromOutcome, outcomeLink, outcomeValue)
            }
            @JvmStatic
            fun fromPredetermining(predetermining : DecisionTreeNode) : InfoSource {
                return InfoSource(SourceType.fromPredetermining, predetermining, null)
            }
            @JvmStatic
            fun fromBranch(branch : ThoughtBranch) : InfoSource {
                return InfoSource(SourceType.fromBranch, branch, null)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InfoSource

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
}