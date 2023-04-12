package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

class GetByCondition(
    args: List<Operator>,
    val varName: String
) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(Types.Boolean))

    val conditionExpr get() = arg(0)


    override val resultDataType get() = Types.Object


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

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}