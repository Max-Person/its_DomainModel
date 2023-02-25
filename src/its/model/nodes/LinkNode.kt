package its.model.nodes

import kotlin.reflect.KClass

sealed class LinkNode<AnswerType : Any> : DecisionTreeNode() {
    abstract val next : Outcomes<AnswerType>
    val children
        get() = next.values
    val answerType : KClass<out AnswerType>
        get(){
            val clazz = next.keys.first()::class
            require(next.keys.all{it::class == clazz}){
                "Не поддерживаются узлы, где классы ответов различаются"
            }
            return clazz
        }
}