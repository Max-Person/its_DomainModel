package its.model.nodes.visitors

import its.model.nodes.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в узлы дерева решений (подклассы [DecisionTreeNode])
 * @param Info тип возвращаемого функциями поведения значения
 * @see DecisionTreeNode.use
 */
interface DecisionTreeBehaviour<Info> : LinkNodeBehaviour<Info> {
    // ---------------------- Для узлов дерева решений ---------------------------
    fun process(node: BranchResultNode) : Info
    fun process(node: StartNode) : Info
    fun process(branch: ThoughtBranch) : Info
    fun process(node: UndeterminedResultNode) : Info
}