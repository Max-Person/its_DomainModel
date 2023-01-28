package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor

/**
 * Сравнение с явным указанием оператора
 * TODO?: сравнение объектов на больше/меньше?
 */
class CompareWithComparisonOperator(
    args: List<Operator>,
    private var operator: ComparisonOperator
) : BaseOperator(args) {

    sealed class ComparisonOperator {

        /**
         * Меньше
         */
        object Less : ComparisonOperator()

        /**
         * Больше
         */
        object Greater : ComparisonOperator()

        /**
         * Равно
         */
        object Equal : ComparisonOperator()

        /**
         * Меньше или равно
         */
        object LessEqual : ComparisonOperator()

        /**
         * Больше или равно
         */
        object GreaterEqual : ComparisonOperator()

        /**
         * Не равно
         */
        object NotEqual : ComparisonOperator()

        companion object {

            fun values(): Array<ComparisonOperator> {
                return arrayOf(Less, Greater, Equal, LessEqual, GreaterEqual, NotEqual)
            }

            fun valueOf(value: String) = when (value) {
                "LESS" -> Less
                "GREATER" -> Greater
                "EQUAL" -> Equal
                "LESS_EQUAL" -> LessEqual
                "GREATER_EQUAL" -> Greater
                "NOT_EQUAL" -> NotEqual
                else -> null
            }
        }
    }

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    init {
        require(!(
                (arg(0).resultDataType == DataType.String
                        || arg(0).resultDataType == DataType.Object)
                        && (operator != ComparisonOperator.Equal
                        || operator != ComparisonOperator.NotEqual)
                )) { "Указанный оператор не совместим с этими типам данных" }
    }

    override val argsDataTypes get() = listOf(
        listOf(DataType.Integer, DataType.Double),
        listOf(DataType.Double, DataType.Integer),
        listOf(DataType.Integer, DataType.Integer),
        listOf(DataType.Double, DataType.Double),
        listOf(DataType.String, DataType.String),
        listOf(DataType.Object, DataType.Object),
        listOf(DataType.Enum, DataType.Enum)
    )


    override val resultDataType get() = DataType.Boolean


    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return CompareWithComparisonOperator(newArgs, operator)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CompareWithComparisonOperator(newArgs, operator)
    }

    override fun <I> accept(visitor: OperatorVisitor<I>): I {
        return visitor.process(this, visitor.process(this), args.map { it.accept(visitor) })
    }

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}