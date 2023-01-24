package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Оператор проверки класса объекта
 */
class CheckClass(args: List<Operator>) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes get() = listOf(listOf(DataType.Object, DataType.Class))

    override val resultDataType get() = DataType.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return CheckClass(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CheckClass(newArgs)
    }
}