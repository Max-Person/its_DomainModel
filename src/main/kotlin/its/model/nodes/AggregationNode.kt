package its.model.nodes

/**
 * Общий класс для узлов логической агрегации
 */
sealed class AggregationNode : LinkNode<Boolean>() {
    abstract val logicalOp: LogicalOp
}