package its.model.nodes.visitors

import its.model.ValueTuple
import its.model.nodes.*

/**
 * Интерфейс, описывающий некоторое поведение, внедряемое в узлы дерева решений (подклассы [DecisionTreeNode])
 *
 * Определяет общую функцию process для всех [LinkNode]
 * @param Info тип возвращаемого функциями поведения значения
 * @see DecisionTreeBehaviour
 */
interface SimpleDecisionTreeBehaviour<out Info> : DecisionTreeBehaviour<Info> {
    override fun process(node: CycleAggregationNode): Info {
        return process(node as LinkNode<Boolean>)
    }

    override fun process(node: WhileCycleNode): Info {
        return process(node as LinkNode<Boolean>)
    }

    override fun process(node: FindActionNode): Info {
        return process(node as LinkNode<Boolean>)
    }

    override fun process(node: BranchAggregationNode): Info {
        return process(node as LinkNode<Boolean>)
    }

    override fun process(node: QuestionNode): Info {
        return process(node as LinkNode<Any>)
    }

    override fun processTupleQuestionNode(node: TupleQuestionNode): Info {
        return process(node as LinkNode<ValueTuple>)
    }

    fun <AnswerType : Any> process(node: LinkNode<AnswerType>): Info
}