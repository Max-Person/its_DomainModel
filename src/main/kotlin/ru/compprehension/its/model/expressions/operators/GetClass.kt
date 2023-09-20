package ru.compprehension.its.model.expressions.operators

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить класс объекта
 */
class GetClass(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(Types.Object))

    val objectExpr get() = arg(0)


    override val resultDataType get() = Types.Class


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

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}