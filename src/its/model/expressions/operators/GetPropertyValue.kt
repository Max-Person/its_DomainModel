package its.model.expressions.operators

import its.model.DomainModel
import its.model.expressions.Operator
import its.model.expressions.literals.PropertyRef
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Получить значение свойства объекта
 */
class GetPropertyValue(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes
        get() = listOf(listOf(Types.Object, PropertyRef::class))

    override val resultDataType get() = DomainModel.propertiesDictionary.dataType((args[1] as PropertyRef).name)!!


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetPropertyValue(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetPropertyValue(newArgs)
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}