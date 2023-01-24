package its.model.expressions

import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil

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

    override fun compile(): CompilationResult =
        CompilationResult(value = JenaUtil.genVar(name))

    override fun clone(): Operator = Variable(name)

    override fun clone(newArgs: List<Operator>): Operator {
        require(newArgs.isEmpty()) { "Для переменной аргументы не требуются." }
        return clone()
    }
}