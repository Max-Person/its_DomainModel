package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.BooleanLiteral
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.genCountValuesPrim
import its.model.util.JenaUtil.genEqualPrim
import its.model.util.JenaUtil.genNoValuePrim
import its.model.util.JenaUtil.genRule
import its.model.util.JenaUtil.genTriple
import its.model.util.JenaUtil.genVal
import its.model.util.JenaUtil.genVar
import its.model.util.NamingManager.genPredicateName
import its.model.util.NamingManager.genVarName

/**
 * Выбор экстремального объекта
 */
class GetExtreme(
    args: List<Operator>,
    val varName: String,
    val extremeVarName: String
): BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Boolean, DataType.Boolean))


    override val resultDataType = DataType.Object


    override fun compile(): CompilationResult {
        // Объявляем переменные
        val value = genVarName()
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

        // Вспомогательные переменные
        val empty0 = genVarName()
        val empty1 = genVarName()

        // Флаг, указывающий на объекты множества
        val flag = genPredicateName()
        // Skolem name
        val skolemName = genVarName()

        // Флаг цикла
        val cycleFlag = genPredicateName()
        // Переменная цикла
        val cycleVar = genVarName()

        // Для всех результатов компиляции
        compiledArg0.heads.forEach { head0 ->
            compiledArg1.heads.forEach { head1 ->

                // ---------------- Генерируем правило, помечающее объекты множества --------------

                // Инициализируем переменную FIXME?: сюда не попадут объекты без связей
                var tmpHead0 = genTriple(genVar(varName), genVarName(), genVarName())

                tmpHead0 += head0

                // Если оператор булево значение
                if (arg0 is BooleanLiteral) {
                    // Добавляем выражение, равное значению
                    tmpHead0 += if (arg0.value.toBoolean()) {
                        genEqualPrim("1", "1")
                    } else {
                        genEqualPrim("0", "1")
                    }
                }

                // Добавляем в рзультат
                completedRules += genRule(tmpHead0, skolemName, flag, genVar(varName))

                // ---------------- Генерируем правило, помечающее потенциальный экстремум --------------

                // Собираем правило, организующее цикл
                val cycleHead = genNoValuePrim(empty0, cycleFlag) +
                        genTriple(empty1, flag, cycleVar)

                // Добавляем в рзультат
                completedRules += genRule(cycleHead, skolemName, cycleFlag, cycleVar)

                // ---------------- Генерируем правило, проверяющее экстремум --------------

                var tmpHead1 = head1

                // Если оператор булево значение
                if (arg1 is BooleanLiteral) {
                    // Добавляем выражение, равное значению
                    tmpHead1 += if (arg1.value.toBoolean()) {
                        genEqualPrim("1", "1")
                    } else {
                        genEqualPrim("0", "1")
                    }
                }

                // Собираем фильтрующее правило
                val filterHead = genTriple(empty0, cycleFlag, genVar(extremeVarName)) + tmpHead1

                var filterRule = EXTREME_PATTERN
                filterRule = filterRule.replace("<ruleHead>", filterHead)

                // Добавляем в основное правило получение объекта и проверку их количества
                val mainHead = genTriple(empty0, cycleFlag, value) +
                        genCountValuesPrim(empty0, cycleFlag, empty1) +
                        genEqualPrim(empty1, genVal(1))

                // Добавляем в рзультат
                heads.add(mainHead)
                completedRules += filterRule
            }
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

        return GetExtreme(newArgs, varName, extremeVarName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetExtreme(newArgs, varName, extremeVarName)
    }

    companion object {

        /**
         * Шаблон правила выбора экстремального
         */
        private const val EXTREME_PATTERN = "[<ruleHead>->drop(0)]"
    }
}