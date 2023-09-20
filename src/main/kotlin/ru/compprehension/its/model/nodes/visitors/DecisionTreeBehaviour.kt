package ru.compprehension.its.model.nodes.visitors

import ru.compprehension.its.model.nodes.BranchResultNode
import ru.compprehension.its.model.nodes.StartNode
import ru.compprehension.its.model.nodes.ThoughtBranch
import ru.compprehension.its.model.nodes.UndeterminedResultNode

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в узлы дерева решений (подклассы [DecisionTreeNode])
 * @param Info тип возвращаемого функциями поведения значения
 * @see DecisionTreeNode.use
 */
interface DecisionTreeBehaviour<Info> : LinkNodeBehaviour<Info> {
    // ---------------------- Для узлов дерева решений ---------------------------
    fun process(node: BranchResultNode): Info
    fun process(node: StartNode): Info
    fun process(branch: ThoughtBranch): Info
    fun process(node: UndeterminedResultNode): Info
}