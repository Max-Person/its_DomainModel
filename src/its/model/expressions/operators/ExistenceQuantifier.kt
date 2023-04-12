package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Квантор существования
 * TODO: отрицание
 */
class ExistenceQuantifier(
    args: List<Operator>,
    val varName: String
) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes get() = listOf(listOf(Types.Boolean))

    val conditionExpr get() = arg(0)

    override val resultDataType get() = Types.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return ExistenceQuantifier(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return ExistenceQuantifier(newArgs, varName)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}