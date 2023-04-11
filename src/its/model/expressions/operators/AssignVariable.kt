package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.DecisionTreeVar
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

class AssignVariable(args: List<Operator>) : BaseOperator(args){

    override val argsDataTypes
        get() = listOf(listOf(DecisionTreeVar::class, Types.Object))

    override val resultDataType
        get() = Types.None

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return AssignProperty(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return AssignProperty(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}