package its.model.nodes.visitors

import its.model.nodes.BranchResultNode
import its.model.nodes.ThoughtBranch

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в узлы дерева решений (подклассы [DecisionTreeNode])
 * @param Info тип возвращаемого функциями поведения значения
 * @see DecisionTreeNode.use
 */
interface DecisionTreeBehaviour<out Info> : LinkNodeBehaviour<Info> {
    // ---------------------- Для узлов дерева решений ---------------------------
    fun process(node: BranchResultNode): Info
    fun process(branch: ThoughtBranch): Info
}