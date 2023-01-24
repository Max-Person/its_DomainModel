package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Квантор общности
 */
class ForAllQuantifier(
    args: List<Operator>,
    private val varName: String
) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes = listOf(listOf(DataType.Boolean, DataType.Boolean))


    override val resultDataType = DataType.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return ForAllQuantifier(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return ForAllQuantifier(newArgs, varName)
    }
}