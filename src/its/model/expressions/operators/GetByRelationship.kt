package its.model.expressions.operators

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.dictionaries.RelationshipsDictionary
import its.model.dictionaries.RelationshipsDictionary.args
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.genCountValuesPrim
import its.model.util.JenaUtil.genEqualPrim
import its.model.util.JenaUtil.genRule
import its.model.util.JenaUtil.genTriple
import its.model.util.JenaUtil.genVal
import its.model.util.NamingManager
import its.model.util.NamingManager.genVarName

/**
 * Получить объект по отношению
 * TODO: еще bool условие
 */
class GetByRelationship(
    args: List<Operator>,
    private val varName: String?
) : BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Object, DataType.Relationship))


    override val resultDataType = DataType.Object


    override fun compile(): CompilationResult {
        // Объявляем переменные
        val value = genVarName()
        val heads = ArrayList<String>()
        var completedRules = ""

        // Получаем аргументы
        val arg0 = arg(0)
        val arg1 = arg(1)

        // Проверяем бинарность отношения
        val relName = (arg0 as Literal).value
        require(args(relName)!!.size == 2) { "Отношение не является бинарным" }

        // Компилируем аргументы
        val compiledArg0 = arg0.compile()
        val compiledArg1 = arg1.compile()

        // Передаем завершенные правила дальше
        completedRules += compiledArg0.rules +
                compiledArg1.rules + RelationshipsDictionary.rules(relName)

        // Флаг, указывающий на объекты множества
        val flag = NamingManager.genPredicateName()
        // Skolem name
        val skolemName = genVarName()

        // Вспомогательные переменные
        val empty0 = genVarName()
        val empty1 = genVarName()

        // Для всех результатов компиляции
        compiledArg0.heads.forEach { head0 ->
            compiledArg1.heads.forEach { head1 ->
                var head = head0 + head1

                // Получаем шаблон отношения и заполняем его
                var patternHead = RelationshipsDictionary.head(relName)!!
                patternHead = patternHead.replace("<arg1>", compiledArg0.value)
                patternHead = patternHead.replace("<arg2>", value)

                val varCount = RelationshipsDictionary.varsCount(relName)!!
                for (i in 1..varCount) {
                    patternHead = patternHead.replace("<var$i>", genVarName())
                }

                head += patternHead

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

        return GetByRelationship(newArgs, varName)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetByRelationship(newArgs, varName)
    }
}