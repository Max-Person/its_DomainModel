package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Проверка наличия отношения между объектами
 * TODO?: переходы между классами?
 * TODO: negative form
 */
// TODO relation type
class CheckRelationship(args: List<Operator>) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes get() = listOf(
        listOf(DataType.Object, DataType.Relationship, DataType.Object)
    )


    override val isArgsCountUnlimited: Boolean
        get() = true

    override val resultDataType get() = DataType.Boolean


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return CheckRelationship(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CheckRelationship(newArgs)
    }

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}