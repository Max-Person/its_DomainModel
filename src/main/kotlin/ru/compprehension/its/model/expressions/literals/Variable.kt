package ru.compprehension.its.model.expressions.literals

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.LiteralBehaviour

/**
 * Переменная, вводимая некоторыми операторами
 * @param name Имя переменной
 */
class Variable(
    name: String
) : ReferenceLiteral(name) {

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