package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.BooleanLiteral
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil.genEqualPrim

/**
 * Логическое И
 */
class LogicalAnd(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Boolean, DataType.Boolean))

    override val resultDataType get() = DataType.Boolean
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

        // Если операторы - булевы значения
        when {
            arg0 is BooleanLiteral && arg1 is BooleanLiteral -> {
                // Добавляем выражение, равное значению
                val head = if (arg0.value.toBoolean() && arg1.value.toBoolean()) {
                    genEqualPrim("1", "1")
                } else {
                    genEqualPrim("0", "1")
                }

                // Добавляем в массив
                heads.add(head)
            }

            arg0 is BooleanLiteral -> {
                // Добавляем выражение, равное значению
                val head0 = if (arg0.value.toBoolean()) {
                    genEqualPrim("1", "1")
                } else {
                    genEqualPrim("0", "1")
                }

                // Для всех результатов компиляции
                compiledArg1.heads.forEach { head1 ->
                    val head = head0 + head1

                    // Добавляем в массив
                    heads.add(head)
                }
            }

            arg1 is BooleanLiteral -> {
                // Добавляем выражение, равное значению
                val head1 = if (arg1.value.toBoolean()) {
                    genEqualPrim("1", "1")
                } else {
                    genEqualPrim("0", "1")
                }

                // Для всех результатов компиляции
                compiledArg0.heads.forEach { head0 ->
                    val head = head0 + head1

                    // Добавляем в массив
                    heads.add(head)
                }
            }
            else -> {
                // Для всех результатов компиляции
                compiledArg0.heads.forEach { head0 ->
                    compiledArg1.heads.forEach { head1 ->
                        val head = head0 + head1

                        // Добавляем в массив
                        heads.add(head)
                    }
                }
            }
        }

        return CompilationResult("", heads, completedRules)
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return LogicalAnd(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return LogicalAnd(newArgs)
    }
}