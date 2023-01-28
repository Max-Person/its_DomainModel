package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.visitors.OperatorBehaviour
import its.model.visitors.OperatorVisitor

/**
 * Получить класс объекта
 */
class GetClass(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Object))


    override val resultDataType get() = DataType.Object


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetClass(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetClass(newArgs)
    }

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}