package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Последовательное выполнение нескольких операторов
 */
class Block(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Any),
        )

    override val isArgsCountUnlimited: Boolean
        get() = true

    override val resultDataType
        get() = Types.None

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return Block(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return Block(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}