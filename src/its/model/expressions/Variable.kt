package its.model.expressions

import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Переменная, вводимая некоторыми операторами
 * @param name Имя переменной
 */
class Variable(
    private val name: String
) : Operator {

    override val args: List<Operator>
        get() = ArrayList()

    override val argsDataTypes: List<List<DataType>>
        get() = ArrayList()

    override val resultDataType: DataType
        get() = DataType.Object

    override fun clone(): Operator = Variable(name)

    override fun clone(newArgs: List<Operator>): Operator {
        require(newArgs.isEmpty()) { "Для переменной аргументы не требуются." }
        return clone()
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}