package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

class GetByCondition(
    args: List<Operator>,
    val varName: String
) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Boolean))


    override val resultDataType get() = DataType.Object


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetByCondition(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetByCondition(newArgs, varName)
    }

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}