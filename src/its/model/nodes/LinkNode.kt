package its.model.nodes

sealed interface LinkNode {
    val children : List<DecisionTreeNode>
}