package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.util.DataType

/**
 * Оператор проверки значения свойства объекта
 */
class CheckPropertyValue(args: List<Operator>) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes
        get() = listOf(
            listOf(DataType.Object, DataType.Property, DataType.Integer),
            listOf(DataType.Object, DataType.Property, DataType.Double),
            listOf(DataType.Object, DataType.Property, DataType.Boolean),
            listOf(DataType.Object, DataType.Property, DataType.String),
            listOf(DataType.Object, DataType.Property, DataType.Enum)
        )

    override val resultDataType get() = DataType.Boolean

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return CheckPropertyValue(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CheckPropertyValue(newArgs)
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

        /**
         * Имя отношения, используемого при вычислении самого удаленного класса
         */
        private const val RELATIONSHIP_NAME = "isFurtherFromThan"

        /**
         * Шаблон правила выбора экстремального класса
         */
        private const val EXTREME_CLASS_PATTER = "[<ruleHead>->drop(0)]"
    }
}