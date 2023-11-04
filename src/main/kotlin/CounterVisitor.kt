import its.model.nodes.*

class CounterVisitor{
    var count = 0
    var tabs = ""

    fun process(node: DecisionTreeNode) {
        count++
        println("$tabs$count : ${node.javaClass.simpleName} ${node.additionalInfo.get("alias")?: "" }")
        if(node is LinkNode<*>){
            tabs+="| "
            if(node is LogicAggregationNode)
                node.thoughtBranches.forEach { process(it) }
            else if(node is CycleAggregationNode)
                process(node.thoughtBranch)
            else if(node is WhileAggregationNode)
                process(node.thoughtBranch)

            if(node is PredeterminingFactorsNode){
                node.next.info.forEach {
                    if(it.decidingBranch != null) process(it.decidingBranch!!)
                    process(it.value)
                }
            }
            else
                node.children.forEach { process(it)}
            tabs = tabs.dropLast(2)
        }
        else if(node is StartNode){
            process(node.main)
        }
    }

    fun process(branch: ThoughtBranch) {
        println("${tabs}")
        println("${tabs}New ThoughtBranch ${branch.additionalInfo.get("alias")?: "" }")
        tabs+="| "
        process(branch.start)
        tabs = tabs.dropLast(2)
        println("${tabs}")
    }
}