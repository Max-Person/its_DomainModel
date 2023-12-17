package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Условное выполнение вложенного оператора
 */
class IfThen(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Boolean, Types.Any),
        )

    val conditionExpr get() = arg(0)
    val thenExpr get() = arg(1)

    override val resultDataType
        get() = Types.None

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return IfThen(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return IfThen(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}