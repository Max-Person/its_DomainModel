package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil.genEqualPrim
import its.model.util.JenaUtil.genGreaterEqualPrim
import its.model.util.JenaUtil.genGreaterThanPrim
import its.model.util.JenaUtil.genLessEqualPrim
import its.model.util.JenaUtil.genLessThanPrim
import its.model.util.JenaUtil.genNotEqualPrim

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

    override val argsDataTypes = listOf(
        listOf(DataType.Integer, DataType.Double),
        listOf(DataType.Double, DataType.Integer),
        listOf(DataType.Integer, DataType.Integer),
        listOf(DataType.Double, DataType.Double),
        listOf(DataType.String, DataType.String),
        listOf(DataType.Object, DataType.Object)
    )


    override val resultDataType = DataType.Boolean


    override fun compile(): CompilationResult {
        // Объявляем переменные
        val heads = ArrayList<String>()
        var completedRules = ""

        // Получаем аргументы
        val arg0 = arg(0)
        val arg1 = arg(1)

        // Компилируем аргументы
        val compiledArg0 = arg0.compile()
        val compiledArg1 = arg1.compile()

        // Передаем завершенные правила дальше
        completedRules += compiledArg0.rules +
                compiledArg1.rules

        // Если нужно отрицание
        if (isNegative) {
            // Меням оператор на противоположный
            operator = when (operator) {
                ComparisonOperator.Equal -> ComparisonOperator.NotEqual
                ComparisonOperator.Greater -> ComparisonOperator.LessEqual
                ComparisonOperator.GreaterEqual -> ComparisonOperator.Less
                ComparisonOperator.Less -> ComparisonOperator.GreaterEqual
                ComparisonOperator.LessEqual -> ComparisonOperator.Greater
                ComparisonOperator.NotEqual -> ComparisonOperator.Equal
            }
        }

        // Для всех результатов компиляции
        compiledArg0.heads.forEach { head0 ->
            compiledArg1.heads.forEach { head1 ->
                var head = head0 + head1

                // Добавляем проверку соответствующего оператору примитива
                head += when (operator) {
                    ComparisonOperator.Equal -> {
                        genEqualPrim(compiledArg0.value, compiledArg1.value)
                    }

                    ComparisonOperator.Greater -> {
                        genGreaterThanPrim(compiledArg0.value, compiledArg1.value)
                    }
                    ComparisonOperator.GreaterEqual -> {
                        genGreaterEqualPrim(compiledArg0.value, compiledArg1.value)
                    }
                    ComparisonOperator.Less -> {
                        genLessThanPrim(compiledArg0.value, compiledArg1.value)
                    }
                    ComparisonOperator.LessEqual -> {
                        genLessEqualPrim(compiledArg0.value, compiledArg1.value)
                    }
                    ComparisonOperator.NotEqual -> {
                        genNotEqualPrim(compiledArg0.value, compiledArg1.value)
                    }
                }

                // Добавляем в рзультат
                heads.add(head)
            }
        }

        return CompilationResult("", heads, completedRules)
    }

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
}