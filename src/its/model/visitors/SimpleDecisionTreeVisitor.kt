package its.model.visitors

import its.model.nodes.*
import its.model.visitors.DecisionTreeVisitor.InfoSource

/**
 * Простая реализация интерфейса [DecisionTreeVisitor] для случаев,
 * когда логика обработки разных типов узлов не так важна, как факт обхода дерева решений.
 * @see DecisionTreeVisitor
 */
abstract class SimpleDecisionTreeVisitor<Info> : DecisionTreeVisitor<Info>  {
    // ---------------------- Для узлов дерева решений ---------------------------
    // ---------------------- До обработки дочерних узлов ------------------------
    override fun process(node: BranchResultNode) : Info     { return process(node as DecisionTreeNode) }
    override fun process(node: CycleAggregationNode) : Info { return process(node as DecisionTreeNode) }
    override fun process(node: FindActionNode) : Info       { return process(node as DecisionTreeNode) }
    override fun process(node: LogicAggregationNode) : Info { return process(node as DecisionTreeNode) }
    override fun process(node: PredeterminingFactorsNode) : Info { return process(node as DecisionTreeNode) }
    override fun process(node: QuestionNode) : Info         { return process(node as DecisionTreeNode) }
    override fun process(node: StartNode) : Info            { return process(node as DecisionTreeNode) }
    override fun process(node: UndeterminedResultNode) : Info   { return process(node as DecisionTreeNode) }

    abstract override fun process(branch: ThoughtBranch) : Info
    abstract fun process(node: DecisionTreeNode) : Info

    // ---------------------- Для узлов дерева выражений ---------------------------
    // -------------------- После обработки дочерних узлов -------------------------
    override fun process(node: CycleAggregationNode, info: Map<InfoSource, Info>) : Info { return process(node as DecisionTreeNode, info) }
    override fun process(node: FindActionNode,       info: Map<InfoSource, Info>) : Info { return process(node as DecisionTreeNode, info) }
    override fun process(node: LogicAggregationNode, info: Map<InfoSource, Info>) : Info { return process(node as DecisionTreeNode, info) }
    override fun process(node: PredeterminingFactorsNode, info: Map<InfoSource, Info>) : Info { return process(node as DecisionTreeNode, info) }
    override fun process(node: QuestionNode,         info: Map<InfoSource, Info>) : Info { return process(node as DecisionTreeNode, info) }
    override fun process(node: StartNode,            info: Map<InfoSource, Info>) : Info { return process(node as DecisionTreeNode, info) }

    abstract override fun process(branch: ThoughtBranch,      info: Map<InfoSource, Info>) : Info
    abstract fun process(node: DecisionTreeNode,     info: Map<InfoSource, Info>) : Info
}