package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.BooleanLiteral
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.genCountValuesPrim
import its.model.util.JenaUtil.genEqualPrim
import its.model.util.JenaUtil.genRule
import its.model.util.JenaUtil.genTriple
import its.model.util.JenaUtil.genVal
import its.model.util.JenaUtil.genVar
import its.model.util.NamingManager.genPredicateName
import its.model.util.NamingManager.genVarName

class GetByCondition(
    args: List<Operator>,
    val varName: String
) : BaseOperator(args) {

    override val argsDataTypes get() = listOf(listOf(DataType.Boolean))


    override val resultDataType get() = DataType.Object


    override fun compile(): CompilationResult {
        // Объявляем переменные
        val value = genVar(varName)
        val heads = ArrayList<String>()
        var completedRules = ""

        // Получаем аргументы
        val arg0 = arg(0)

        // Компилируем аргументы
        val compiledArg0 = arg0.compile()

        // Передаем завершенные правила дальше
        completedRules += compiledArg0.rules

        // Флаг, указывающий на объекты множества
        val flag = genPredicateName()
        // Skolem name
        val skolemName = genVarName()

        // Вспомогательные переменные
        val empty0 = genVarName()
        val empty1 = genVarName()

        // Для всех результатов компиляции
        compiledArg0.heads.forEach { head0 ->
            // Инициализируем переменную FIXME?: сюда не попадут объекты без связей
            var head = genTriple(value, genVarName(), genVarName())

            head += head0

            // Если оператор булево значение
            if (arg0 is BooleanLiteral) {
                // Добавляем выражение, равное значению
                head += if (arg0.value.toBoolean()) {
                    genEqualPrim("1", "1")
                } else {
                    genEqualPrim("0", "1")
                }
            }

            // Собираем правило, помачающее найденные объекты
            val rule = genRule(head, skolemName, flag, value)

            // Добавляем в основное правило
            val mainHead = genTriple(empty0, flag, value) +
                    genCountValuesPrim(empty0, flag, empty1) +
                    genEqualPrim(empty1, genVal(1))

            // Добавляем в рзультат
            heads.add(mainHead)
            completedRules += rule
        }

        // Добавляем паузу
        completedRules += JenaUtil.PAUSE_MARK

        return CompilationResult(value, heads, completedRules)
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetByCondition(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetByCondition(newArgs, varName)
    }
}