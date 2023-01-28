package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Получить объект по отношению
 * TODO: еще bool условие
 */
class GetByRelationship(
    args: List<Operator>,
    private val varName: String?
) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Object, DataType.Relationship))


    override val resultDataType get() = DataType.Object


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetByRelationship(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetByRelationship(newArgs, varName)
    }

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}