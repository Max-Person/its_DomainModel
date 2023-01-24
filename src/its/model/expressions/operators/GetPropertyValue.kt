package its.model.expressions.operators

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.util.CompilationResult
import its.model.dictionaries.PropertiesDictionary
import its.model.dictionaries.PropertiesDictionary.isPropertyBeingOverridden
import its.model.dictionaries.PropertiesDictionary.isStatic
import its.model.dictionaries.RelationshipsDictionary
import its.model.dictionaries.util.DictionariesUtil.SUBCLASS_SCALE_PREDICATE
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.genLink
import its.model.util.JenaUtil.genNoValuePrim
import its.model.util.JenaUtil.genRule
import its.model.util.JenaUtil.genTriple
import its.model.util.NamingManager.genPredicateName
import its.model.util.NamingManager.genVarName

/**
 * Получить значение свойства объекта
 */
class GetPropertyValue(args: List<Operator>) : BaseOperator(args) {

    override val argsDataTypes = listOf(listOf(DataType.Object, DataType.Property))

    override val resultDataType = PropertiesDictionary.dataType((args[3] as Literal).value)


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

        // Имя свойства
        val propName = (arg1 as Literal).value

        // Если не свойство статическое
        if (isStatic(propName) == false) {
            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->
                    var head = head0 + head1

                    // Добавляем проверку свойства
                    head += genTriple(
                        compiledArg0.value,
                        compiledArg1.value,
                        value
                    )

                    // Добавляем в массив
                    heads.add(head)
                }
            }
        }
        // Проверяем, переопределяется ли свойство
        else if (isPropertyBeingOverridden(propName)) {
            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules

            // Вспомогательные переменные
            val empty0 = genVarName()
            val empty1 = genVarName()

            // Флаг, указывающий на классы объекта с заданным свойством
            val classWithPropFlag = genPredicateName()
            // Переменная с классом
            val classVar = genVarName()
            // Skolem name
            val skolemName = genVarName()

            // Флаг цикла
            val cycleFlag = genPredicateName()
            // Переменная цикла
            val cycleVar = genVarName()

            // Переменные аргументов
            val ruleArg1 = genVarName()
            val ruleArg2 = genVarName()
            val ruleArg3 = genVarName()

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->

                    // ---------------- Генерируем правило, помечающее классы объекта --------------

                    var head = head0 + head1

                    // Получаем класс
                    head += genTriple(
                        compiledArg0.value,
                        genLink(JenaUtil.RDF_PREF, CLASS_PREDICATE_NAME),
                        classVar
                    )

                    // Добавляем проверку наличия свойства
                    head += genTriple(
                        classVar,
                        compiledArg1.value,
                        empty0
                    )

                    // Добавляем в рзультат
                    completedRules += genRule(head, skolemName, classWithPropFlag, classVar)

                    // ---------------- Генерируем правило, помечающее потенциальный экстремум --------------

                    // Собираем правило, организующее цикл
                    val cycleHead = genNoValuePrim(empty0, cycleFlag) +
                            genTriple(empty1, classWithPropFlag, cycleVar)

                    // Добавляем в рзультат
                    completedRules += genRule(cycleHead, skolemName, cycleFlag, cycleVar)

                    // ---------------- Генерируем правило, проверяющее экстремум --------------

                    // Собираем правило, выбирающее ближайший класс

                    // FIXME:? два класса с указанным свойством на двух ветках?
                    // Инициализируем аргумент 1 - элемент цикла
                    var filterHead = genTriple(empty0, cycleFlag, ruleArg1)
                    // Инициализируем аргумент 2 - самый удаленный от объекта класс (типа Object в Java)
                    filterHead += genNoValuePrim(ruleArg2, genLink(JenaUtil.RDF_PREF, SUBCLASS_PREDICATE_NAME))
                    // Инициализируем аргумент 3 - класс со свойством
                    filterHead += genTriple(empty1, classWithPropFlag, ruleArg3)

                    // Вычисляем самый удаленный класс
                    // Получаем шаблон
                    // FIXME хардкод
                    var relPattern = RelationshipsDictionary.LinerScalePatterns.IS_FURTHER_FROM_THAN_PATTERN
                    relPattern =
                        relPattern.replace("<numberPredicate>", genLink(JenaUtil.POAS_PREF, SUBCLASS_SCALE_PREDICATE))

                    var patternHead = relPattern
                    // Заполнеяем аргументы
                    patternHead = patternHead.replace("<arg1>", ruleArg1)
                    patternHead = patternHead.replace("<arg2>", ruleArg2)
                    patternHead = patternHead.replace("<arg3>", ruleArg3)

                    // Заполняем временные переменные
                    val varCount = RelationshipsDictionary.LinerScalePatterns.IS_FURTHER_FROM_THAN_VAR_COUNT
                    for (i in 1..varCount) {
                        patternHead = patternHead.replace("<var$i>", genVarName())
                    }

                    // Добавляем запоненный шаблон
                    filterHead += patternHead

                    // Генерируем правило
                    var filterRule = EXTREME_CLASS_PATTER
                    filterRule = filterRule.replace("<ruleHead>", filterHead)

                    // Добавляем в основное правило
                    val mainHead = genTriple(empty0, cycleFlag, classVar) +
                            genTriple(classVar, compiledArg1.value, value)

                    // Добавляем в рзультат
                    heads.add(mainHead)
                    completedRules += filterRule
                }
            }

            // Добавляем паузу
            completedRules += JenaUtil.PAUSE_MARK
        }
        else {
            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules

            // Временная переменная для класса
            val classVar = genVarName()

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->
                    var head = head0 + head1

                    // Получаем класс
                    head += genTriple(
                        compiledArg0.value,
                        genLink(JenaUtil.RDF_PREF, CLASS_PREDICATE_NAME),
                        classVar
                    )

                    // Добавляем проверку свойства
                    head += genTriple(
                        classVar,
                        compiledArg1.value,
                        value
                    )

                    // Добавляем в массив
                    heads.add(head)
                }
            }
        }

        return CompilationResult(value, heads, completedRules)
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return GetPropertyValue(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return GetPropertyValue(newArgs)
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

        /**
         * Имя отношения, используемого при вычислении самого удаленного класса
         */
        private const val RELATIONSHIP_NAME = "isFurtherFromThan"

        /**
         * Шаблон правила выбора экстремального класса
         */
        private const val EXTREME_CLASS_PATTER = "[\n<ruleHead>\n->\ndrop(0)\n]\n"
    }
}