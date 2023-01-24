package its.model.expressions.operators

import its.model.dictionaries.PropertiesDictionary
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Получить значение свойства объекта
 */
class GetPropertyValue(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Object, DataType.Property))

    override val resultDataType = PropertiesDictionary.dataType((args[3] as Literal).value)


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
}