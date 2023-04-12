package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Оператор проверки класса объекта
 */
class CheckClass(args: List<Operator>) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes get() = listOf(listOf(Types.Object, Types.Class))

    val objectExpr get() = arg(0)
    val classExpr get() = arg(1)

    override val resultDataType get() = Types.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return CheckClass(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CheckClass(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}