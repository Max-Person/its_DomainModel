package ru.compprehension.its.model.expressions.operators

import ru.compprehension.its.model.expressions.Operator
import ru.compprehension.its.model.expressions.literals.RelationshipRef
import ru.compprehension.its.model.expressions.types.Types
import ru.compprehension.its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить объект по отношению
 * TODO: еще bool условие
 * TODO: проверять что отношение бинарное?
 */
class GetByRelationship(
    args: List<Operator>
) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(Types.Object, RelationshipRef::class))

    val subjectExpr get() = arg(0)
    val relationshipName get() = (arg(1) as RelationshipRef).name


    override val resultDataType get() = Types.Object


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetByRelationship(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetByRelationship(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}