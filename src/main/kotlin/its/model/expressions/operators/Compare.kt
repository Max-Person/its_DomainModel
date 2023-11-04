package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Сравнение
 * TODO?: сравнение объектов на больше/меньше?
 */
class Compare(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Integer, Types.Double),
            listOf(Types.Double, Types.Integer),
            listOf(Types.Integer, Types.Integer),
            listOf(Types.Double, Types.Double),
            listOf(Types.String, Types.String),
            listOf(Types.Object, Types.Object),
            listOf(Types.Enum, Types.Enum)
        )

    val firstExpr get() = arg(0)
    val secondExpr get() = arg(1)

    override val resultDataType get() = Types.ComparisonResult

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

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}