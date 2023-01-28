package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.visitors.OperatorBehaviour
import its.model.visitors.OperatorVisitor

/**
 * Сравнение
 * TODO?: сравнение объектов на больше/меньше?
 */
class Compare(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(
        listOf(DataType.Integer, DataType.Double),
        listOf(DataType.Double, DataType.Integer),
        listOf(DataType.Integer, DataType.Integer),
        listOf(DataType.Double, DataType.Double),
        listOf(DataType.String, DataType.String),
        listOf(DataType.Object, DataType.Object),
        listOf(DataType.Enum, DataType.Enum)
    )

    override val resultDataType get() = DataType.ComparisonResult

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

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}