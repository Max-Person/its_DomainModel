package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.RelationshipRef
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

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
        listOf(RelationshipRef::class, Types.Object, Types.Object)
    )

    val relationshipName get() = (arg(0) as RelationshipRef).name
    val subjectExpr get() = arg(1)
    val objectExprs get() = args.subList(2, args.size)


    override val isArgsCountUnlimited: Boolean
        get() = true

    override val resultDataType get() = Types.Boolean


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

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}