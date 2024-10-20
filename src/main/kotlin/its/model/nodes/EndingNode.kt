package its.model.nodes

/**
 * Узел, на котором может остановиться выполнение дерева (или его ветви).
 * При остановке ассоциируется с каким-то результатом [BranchResult].
 */
sealed interface EndingNode {
}