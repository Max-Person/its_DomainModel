package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.BooleanLiteral
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil.genEqualPrim
import its.model.util.JenaUtil.genTriple
import its.model.util.JenaUtil.genVar
import its.model.util.NamingManager.genVarName

/**
 * Квантор существования
 * TODO: отрицание
 */
class ExistenceQuantifier(
    args: List<Operator>,
    private val varName: String
) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes = listOf(listOf(DataType.Boolean))

    override val resultDataType = DataType.Boolean

    override fun compile(): CompilationResult {
        // Объявляем переменные
        val heads = ArrayList<String>()
        var completedRules = ""

        // Получаем аргументы
        val arg0 = arg(0)

        // Компилируем аргументы
        val compiledArg0 = arg0.compile()

        // Передаем завершенные правила дальше
        completedRules += compiledArg0.rules

        // Если оператор булево значение
        if (arg0 is BooleanLiteral) {
            // Добавляем выражение, равное значению
            val head = if (arg0.value.toBoolean()) {
                genEqualPrim("1", "1")
            } else {
                genEqualPrim("0", "1")
            }

            // Добавляем в массив
            heads.add(head)
        } else {
            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                // Добавляем инициализацию переменной
                val head = genTriple(genVar(varName), genVarName(), genVarName()) + head0

                // Добавляем в массив
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

        return ExistenceQuantifier(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return ExistenceQuantifier(newArgs, varName)
    }
}