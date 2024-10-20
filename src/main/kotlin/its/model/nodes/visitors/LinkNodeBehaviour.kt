package its.model.nodes.visitors

import its.model.nodes.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в связующие узлы дерева решений (подклассы [LinkNode])
 * @param Info тип возвращаемого функциями поведения значения
 * @see LinkNode.use
 */
interface LinkNodeBehaviour<out Info> {
    // ---------------------- Для узлов дерева решений ---------------------------
    fun process(node: CycleAggregationNode): Info
    fun process(node: WhileCycleNode): Info
    fun process(node: FindActionNode): Info
    fun process(node: BranchAggregationNode): Info
    fun process(node: QuestionNode): Info
    fun processTupleQuestionNode(node: TupleQuestionNode): Info
}