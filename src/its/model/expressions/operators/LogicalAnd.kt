package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Логическое И
 */
class LogicalAnd(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Boolean, DataType.Boolean))

    override val resultDataType get() = DataType.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return LogicalAnd(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return LogicalAnd(newArgs)
    }
}