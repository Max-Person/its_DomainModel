package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.literals.ClassLiteral
import its.model.expressions.util.CompilationResult
import its.model.dictionaries.ClassesDictionary.calcExpr
import its.model.dictionaries.ClassesDictionary.isCalculable
import its.model.models.ClassModel
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.genBindPrim
import its.model.util.JenaUtil.genLink
import its.model.util.JenaUtil.genNoValuePrim
import its.model.util.JenaUtil.genTriple
import its.model.util.JenaUtil.genVar

/**
 * Оператор проверки класса объекта
 */
class CheckClass(args: List<Operator>) : BaseOperator(args) {

    /**
     * Является ли оператор негативным (т.е. нужно ли отрицание при компиляции)
     */
    internal var isNegative = false

    override val argsDataTypes get() = listOf(listOf(DataType.Object, DataType.Class))

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

        // Если класс можно вычислить (вычисляемый класс можно получить только указав его имя т.е. через ClassValue)
        if (isCalculable((arg1 as ClassLiteral).value)) {
            // Получаем выражение для вычисления
            val calculation = calcExpr(arg1.value)

            // Проверяем корректность словаря
            requireNotNull(calculation) { "Для класса ${arg1.value} в словаре нет выражения" }

            val varName = ClassModel.CALC_EXPR_VAR_NAME
            var expression = calculation

            // Если негативная форма - добавляем отрицание
            if (isNegative) {
                expression = LogicalNot(listOf(expression))
            }

            // Компилируем выражение для вычисления
            val compiledCalculation = expression.semantic().compile()

            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules +
                    compiledCalculation.rules

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->
                    compiledCalculation.heads.forEach { calculationHead ->
                        // Собираем правило
                        var head = head0 + head1 // Собираем части первого и второго аргументов
                        head += genBindPrim(compiledArg0.value, genVar(varName)) // Инициализируем переменную
                        head += calculationHead // Добавляем результат компиляции вычисления

                        // Добавляем в массив
                        heads.add(head)
                    }
                }
            }
        } else {
            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->
                    // Собираем правило
                    var head = head0 + head1 // Собираем части первого и второго аргументов

                    // Добавляем проверку класса
                    head += if (isNegative) {
                        // Если форма негативная - проверяем отсутствие класса
                        genNoValuePrim(
                            compiledArg0.value,
                            genLink(JenaUtil.RDF_PREF, CLASS_PREDICATE_NAME),
                            compiledArg1.value
                        )
                    } else {
                        // Проверяем наличие класса
                        genTriple(
                            compiledArg0.value,
                            genLink(JenaUtil.RDF_PREF, CLASS_PREDICATE_NAME),
                            compiledArg1.value
                        )
                    }

                    // Добавляем в массив
                    heads.add(head)
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

        return CheckClass(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return CheckClass(newArgs)
    }

    companion object {

        /**
         * Имя предиката, используемое при компиляции
         */
        private const val CLASS_PREDICATE_NAME = "type"
    }
}