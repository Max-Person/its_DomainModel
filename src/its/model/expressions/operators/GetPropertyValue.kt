package its.model.expressions.operators

import its.model.DomainModel
import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.visitors.OperatorVisitor

/**
 * Получить значение свойства объекта
 */
class GetPropertyValue(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes
        get() = listOf(listOf(DataType.Object, DataType.Property))

    override val resultDataType get() = DomainModel.propertiesDictionary.dataType((args[1] as Literal).value)


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

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }
}