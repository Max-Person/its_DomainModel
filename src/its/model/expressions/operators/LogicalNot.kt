package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Логическое отрицание
 */
class LogicalNot(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(Types.Boolean))

    override val resultDataType get() = Types.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return LogicalNot(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return LogicalNot(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}