package its.model.nodes.visitors

import its.model.nodes.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в связующие узлы дерева решений (подклассы [LinkNode])
 * @param Info тип возвращаемого функциями поведения значения
 * @see LinkNode.use
 */
interface LinkNodeBehaviour<Info> {
    // ---------------------- Для узлов дерева решений ---------------------------
    fun process(node: CycleAggregationNode) : Info
    fun process(node: FindActionNode) : Info
    fun process(node: LogicAggregationNode) : Info
    fun process(node: PredeterminingFactorsNode) : Info
    fun process(node: QuestionNode) : Info
}