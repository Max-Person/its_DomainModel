package ru.compprehension.its.model.expressions.literals

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.Obj
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.LiteralBehaviour
import kotlin.reflect.KClass

/**
 * Object литерал
 * @param name Имя объекта
 */
class ObjectRef(name: String) : ReferenceLiteral(name) {

    override val resultDataType: KClass<Obj>
        get() = Types.Object

    override fun clone(): Operator = ObjectRef(name)

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}