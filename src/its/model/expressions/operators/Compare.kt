package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Сравнение
 * TODO?: сравнение объектов на больше/меньше?
 */
class Compare(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes = listOf(
        listOf(DataType.Integer, DataType.Double),
        listOf(DataType.Double, DataType.Integer),
        listOf(DataType.Integer, DataType.Integer),
        listOf(DataType.Double, DataType.Double),
        listOf(DataType.String, DataType.String),
        listOf(DataType.Object, DataType.Object)
    )

    override val resultDataType = DataType.ComparisonResult

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return Compare(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return Compare(newArgs)
    }
}