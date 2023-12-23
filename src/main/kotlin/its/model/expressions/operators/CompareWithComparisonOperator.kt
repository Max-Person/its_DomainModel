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

            fun valueOf(value: String) = when (value.uppercase()) {
                "LESS", "LT" -> Less
                "GREATER", "GT" -> Greater
                "EQ", "EQUAL" -> Equal
                "LESSEQ", "LE", "LESSEQUAL", "LESS_EQ", "LESS_EQUAL" -> LessEqual
                "GREATEREQ", "GE", "GREATEREQUAL", "GREATER_EQ", "GREATER_EQUAL" -> Greater
                "NOTEQ", "NE", "NOTEQUAL", "NOT_EQ", "NOT_EQUAL" -> NotEqual
                else -> null
            }
        }
    }

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    init {
        require(
            operator == ComparisonOperator.Equal || operator == ComparisonOperator.NotEqual
                    || firstExpr.resultDataType == Types.Integer || firstExpr.resultDataType == Types.Double
        ) { "Оператор сравнения величины ($operator) не совместим с нечисловыми типами данных (${firstExpr.resultDataType.simpleName})" }
    }

    override val argsDataTypes
        get() = listOf(
            listOf(Types.Integer, Types.Double),
            listOf(Types.Double, Types.Integer),
            listOf(Types.Integer, Types.Integer),
            listOf(Types.Double, Types.Double),
            listOf(Types.Boolean, Types.Boolean),
            listOf(Types.String, Types.String),
            listOf(Types.Object, Types.Object),
            listOf(Types.Class, Types.Class),
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