package ru.compprehension.its.model.nodes

import ru.compprehension.its.model.nodes.visitors.DecisionTreeBehaviour
import ru.compprehension.its.model.nodes.visitors.LinkNodeBehaviour
import kotlin.reflect.KClass

sealed class LinkNode<AnswerType : Any> : DecisionTreeNode() {
    abstract val next: Outcomes<AnswerType>
    val children
        get() = next.values
    val answerType: KClass<out AnswerType>
        get() {
            val clazz = next.keys.first()::class
            require(next.keys.all { it::class == clazz }) {
                "Не поддерживаются узлы, где классы ответов различаются"
            }
            return clazz
        }

    abstract fun <I> use(behaviour: LinkNodeBehaviour<I>): I
    override fun <I> use(behaviour: DecisionTreeBehaviour<I>): I {
        return this.use(behaviour as LinkNodeBehaviour<I>)
    }
}