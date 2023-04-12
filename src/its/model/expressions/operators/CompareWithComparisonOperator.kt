package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.types.Types
import its.model.expressions.visitors.OperatorBehaviour

/**
 * Сравнение с явным указанием оператора
 * TODO?: сравнение объектов на больше/меньше?
 */
class CompareWithComparisonOperator(
    args: List<Operator>,
    var operator: ComparisonOperator
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
                (arg(0).resultDataType == Types.String
                        || arg(0).resultDataType == Types.Object)
                        && (operator != ComparisonOperator.Equal
                        || operator != ComparisonOperator.NotEqual)
                )) { "Указанный оператор не совместим с этими типам данных" }
    }

    override val argsDataTypes get() = listOf(
        listOf(Types.Integer, Types.Double),
        listOf(Types.Double, Types.Integer),
        listOf(Types.Integer, Types.Integer),
        listOf(Types.Double, Types.Double),
        listOf(Types.String, Types.String),
        listOf(Types.Object, Types.Object),
        listOf(Types.Enum, Types.Enum)
    )

    val firstExpr get() = arg(0)
    val secondExpr get() = arg(1)


    override val resultDataType get() = Types.Boolean


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

    override fun <I> use(behaviour: OperatorBehaviour<I>): I {
        return behaviour.process(this)
    }
}