package its.model.expressions.operators

import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.genLink
import its.model.util.JenaUtil.genNoValuePrim
import its.model.util.JenaUtil.genTriple
import its.model.util.NamingManager.genVarName

/**
 * Получить класс объекта
 */
class GetClass(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Object))


    override val resultDataType = DataType.Object


    override fun compile(): CompilationResult {
        // Объявляем переменные
        val value = genVarName()
        val heads = ArrayList<String>()
        var completedRules = ""

        // Получаем аргументы
        val arg0 = arg(0)

        // Компилируем аргументы
        val compiledArg0 = arg0.compile()

        // Передаем завершенные правила дальше
        completedRules += compiledArg0.rules

        // Для всех результатов компиляции
        compiledArg0.heads.forEach { head0 ->
            var head = head0
            head += genTriple(
                compiledArg0.value,
                genLink(JenaUtil.RDF_PREF, CLASS_PREDICATE_NAME),
                value
            ) + genNoValuePrim(
                value,
                genLink(JenaUtil.RDF_PREF, SUBCLASS_PREDICATE_NAME)
            )

            // Добавляем в массив
            heads.add(head)
        }

        return CompilationResult(value, heads, completedRules)
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetClass(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetClass(newArgs)
    }

    companion object {

        /**
         * Имя предиката, используемое при компиляции
         */
        private const val CLASS_PREDICATE_NAME = "type"

        /**
         * Имя предиката, используемое при компиляции
         */
        private const val SUBCLASS_PREDICATE_NAME = "subClassOf"
    }
}