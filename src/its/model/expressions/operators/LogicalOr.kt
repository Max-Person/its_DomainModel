package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType

/**
 * Логическое ИЛИ
 */
class LogicalOr(args: List<Operator>): BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Boolean, DataType.Boolean))

    override val resultDataType get() = DataType.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return LogicalOr(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return LogicalOr(newArgs)
    }
}