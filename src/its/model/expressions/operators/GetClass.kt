package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Получить класс объекта
 */
class GetClass(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Object))


    override val resultDataType = DataType.Object


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

    companion object {

        /**
         * Имя предиката, используемое при компиляции
         */
        private const val CLASS_PREDICATE_NAME = "type"

        /**
         * Имя предиката, используемое при компиляции
         */
        private const val SUBCLASS_PREDICATE_NAME = "subClassOf"
    }
}