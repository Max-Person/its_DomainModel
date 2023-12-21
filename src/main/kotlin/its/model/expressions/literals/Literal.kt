package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.visitors.LiteralBehaviour
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Литерал в выражении, не имеет дочерних выражений
 */
sealed class Literal : Operator() {

    override val children: List<Operator>
        get() = ArrayList()


    abstract fun <I> use(behaviour: LiteralBehaviour<I>): I
    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return use(behaviour as LiteralBehaviour<I>)
    }
}