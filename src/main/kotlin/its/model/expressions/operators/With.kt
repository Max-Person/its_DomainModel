package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Сохранение получаемого объекта в контекстную переменную для дальнейших вычислений
 */
class With(
    val varName: String,
    args: List<Operator>,
) : BaseOperator(args) {

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Object, Block::class),
        )

    val objExpr get() = arg(0)
    val block get() = arg(1)

    override val resultDataType
        get() = Types.None

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return With(varName, newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return With(varName, newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}