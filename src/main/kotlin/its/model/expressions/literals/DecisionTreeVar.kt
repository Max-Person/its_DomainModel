package its.model.expressions.literals

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.LiteralBehaviour

/**
 * Литерал переменной из дерева рассуждения
 * @param name Имя переменной из дерева рассуждения
 */
class DecisionTreeVar(name: String) : ReferenceLiteral(name) {

    override val resultDataType
        get() = Types.Object

    override fun clone(): Operator = Variable(name)

    override fun clone(newArgs: List<Operator>): Operator {
        require(newArgs.isEmpty()) { "Для переменной аргументы не требуются." }
        return clone()
    }

    override fun <I> use(behaviour: LiteralBehaviour<I>): I {
        return behaviour.process(this)
    }
}