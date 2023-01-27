package its.model.visitors

import its.model.nodes.*

sealed interface DecisionTreeVisitor<Info>{
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

    // ---------------------- Для узлов дерева выражений ---------------------------
    // -------------------- После обработки дочерних узлов -------------------------
    fun process(node: CycleAggregationNode, info: Map<DecisionTreeSource, Info>) : Info
    fun process(node: FindActionNode,       info: Map<DecisionTreeSource, Info>) : Info
    fun process(node: LogicAggregationNode, info: Map<DecisionTreeSource, Info>) : Info
    fun process(node: PredeterminingFactorsNode, info: Map<DecisionTreeSource, Info>) : Info
    fun process(node: QuestionNode,         info: Map<DecisionTreeSource, Info>) : Info
    fun process(node: StartNode,            info: Map<DecisionTreeSource, Info>) : Info
    fun process(branch: ThoughtBranch,      info: Map<DecisionTreeSource, Info>) : Info
}

