import its.model.nodes.*

class CounterVisitor{
    var count = 0
    var tabs = ""

    fun process(node: DecisionTreeNode) {
        count++
        println("$tabs$count : $node")
        if (node !is LinkNode<*>) return

        tabs += "| "
        if (node is BranchAggregationNode)
            node.thoughtBranches.forEach { process(it) }
        else if (node is CycleAggregationNode)
            process(node.thoughtBranch)
        else if (node is WhileCycleNode)
            process(node.thoughtBranch)

        node.children.forEach { process(it) }
        tabs = tabs.dropLast(2)
    }

    fun process(branch: ThoughtBranch) {
        println("${tabs}")
        println("${tabs}New ThoughtBranch : $branch")
        tabs+="| "
        process(branch.start)
        tabs = tabs.dropLast(2)
        println("${tabs}")
    }

    fun process(tree: DecisionTree) {
        process(tree.mainBranch)
    }
}