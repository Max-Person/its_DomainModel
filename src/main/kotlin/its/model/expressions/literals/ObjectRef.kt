package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.Obj
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour
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