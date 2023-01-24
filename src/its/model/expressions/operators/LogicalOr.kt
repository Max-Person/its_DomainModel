package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType

/**
 * Логическое ИЛИ
 */
class LogicalOr(args: List<Operator>): BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Boolean, DataType.Boolean))

    override val resultDataType get() = DataType.Boolean

    override fun compile(): CompilationResult {
        // Объявляем переменные
        val heads = ArrayList<String>()
        var completedRules = ""

        // Получаем аргументы
        val arg0 = arg(0)
        val arg1 = arg(1)

        // Раскрываем через And
        val expr0 = LogicalAnd(listOf(arg0.clone(), arg1.clone())).semantic()
        val expr1 = LogicalAnd(listOf(LogicalNot(listOf(arg0.clone())), arg1.clone())).semantic()
        val expr2 = LogicalAnd(listOf(arg0.clone(), LogicalNot(listOf(arg1.clone())))).semantic()

        // Компилируем правила
        val compiledExpr0 = expr0.compile()
        val compiledExpr1 = expr1.compile()
        val compiledExpr2 = expr2.compile()

        // Если в разных вариациях отличаются не только головы
        if (compiledExpr0.rules != compiledExpr1.rules
            || compiledExpr0.rules != compiledExpr2.rules
        ) {
            TODO() // Как то собрать их в одно ???
        } else {
            // Передаем завершенные правила дальше
            completedRules += compiledExpr0.rules
        }

        // Собираем полученные правила
        heads.addAll(compiledExpr0.heads)
        heads.addAll(compiledExpr1.heads)
        heads.addAll(compiledExpr2.heads)

        return CompilationResult("", heads, completedRules)
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return LogicalOr(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return LogicalOr(newArgs)
    }
}