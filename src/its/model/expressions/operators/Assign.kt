package its.model.expressions.operators

import its.model.expressions.Literal
import its.model.expressions.Operator
import its.model.expressions.literals.DoubleLiteral
import its.model.expressions.literals.EnumLiteral
import its.model.expressions.literals.IntegerLiteral
import its.model.expressions.literals.PropertyLiteral
import its.model.expressions.util.CompilationResult
import its.model.dictionaries.PropertiesDictionary
import its.model.util.DataType
import its.model.util.JenaUtil
import its.model.util.JenaUtil.DECISION_TREE_VAR_PREDICATE
import its.model.util.JenaUtil.PAUSE_MARK
import its.model.util.JenaUtil.POAS_PREF
import its.model.util.NamingManager

/**
 * Присваивание
 */
class Assign(args: List<Operator>) : BaseOperator(args) {

    init {
        if (args.size == 3) {
            val arg2 = arg(2)

            val propertyName = (arg(1) as PropertyLiteral).value
            val newValueDataType = arg(2).resultDataType!!

            require(PropertiesDictionary.isStatic(propertyName) == false) {
                "Свойство $propertyName не должно быть статическим."
            }
            require(PropertiesDictionary.dataType(propertyName) == newValueDataType) {
                "Тип данных $newValueDataType не соответствует типу ${PropertiesDictionary.dataType(propertyName)} свойства $propertyName."
            }
            require(arg2 is Literal || arg2 is GetPropertyValue) {
                "Нельзя присвоить свойству динамическое значение." // FIXME?: можно, но тогда не получится его контролировать
            }

            // FIXME?: проверять попадает ли в диапазон значение при arg2 is GetPropertyValue?
            when (arg2) {
                is IntegerLiteral -> {
                    require(PropertiesDictionary.isValueInRange(propertyName, arg2.value.toInt()) == true) {
                        "Значение ${arg2.value.toInt()} вне диапазона значений свойства $propertyName."
                    }
                }

                is DoubleLiteral -> {
                    require(PropertiesDictionary.isValueInRange(propertyName, arg2.value.toDouble()) == true) {
                        "Значение ${arg2.value.toDouble()} вне диапазона значений свойства $propertyName."
                    }
                }

                is EnumLiteral -> {
                    require(PropertiesDictionary.enumName(propertyName)!! == arg2.owner) {
                        "Тип перечисления ${PropertiesDictionary.enumName(propertyName)} свойства $propertyName не соответствует типу перечисления ${arg2.owner} значения."
                    }
                }

                is GetPropertyValue -> {
                    require(
                        PropertiesDictionary.enumName(propertyName)!!
                                == PropertiesDictionary.enumName((arg2.arg(1) as PropertyLiteral).value)!!
                    ) {
                        "Тип перечисления ${PropertiesDictionary.enumName(propertyName)} свойства $propertyName не соответствует типу перечисления ${
                            PropertiesDictionary.enumName(
                                (arg2.arg(1) as PropertyLiteral).value
                            )
                        } значения."
                    }
                }
            }
        }
    }

    override val argsDataTypes
        get() = listOf(
            listOf(DataType.Object, DataType.Property, DataType.Integer),
            listOf(DataType.Object, DataType.Property, DataType.Double),
            listOf(DataType.Object, DataType.Property, DataType.Boolean),
            listOf(DataType.Object, DataType.Property, DataType.String),
            listOf(DataType.Object, DataType.Property, DataType.Enum),
            listOf(DataType.DecisionTreeVar, DataType.Object)
        )

    override val resultDataType
        get() = null

    override fun compile(): CompilationResult {
        // Объявляем переменные
        var completedRules = ""

        if (args.size == 3) {
            // Получаем аргументы
            val arg0 = arg(0)
            val arg1 = arg(1)
            val arg2 = arg(2)

            // Компилируем аргументы
            val compiledArg0 = arg0.compile()
            val compiledArg1 = arg1.compile()
            val compiledArg2 = arg2.compile()

            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules +
                    compiledArg2.rules

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->
                    compiledArg2.heads.forEach { head2 ->
                        // Собираем правила для аргументов
                        val head = head0 + head1 + head2

                        // Заполняем шаблон
                        var rule = PROPERTY_ASSIGN_PATTERN
                        rule = rule.replace("<tmp0>", NamingManager.genVarName())
                        rule = rule.replace("<tmp1>", NamingManager.genVarName())
                        rule = rule.replace("<dropped>", NamingManager.genPredicateName())

                        rule = rule.replace("<ruleHead>", head)
                        rule = rule.replace("<propHead>", head1)
                        rule = rule.replace("<valueHead>", head2)
                        rule = rule.replace("<subjName>", compiledArg0.value)
                        rule = rule.replace("<propName>", compiledArg1.value)
                        rule = rule.replace("<value>", compiledArg2.value)

                        // Добавляем в результат
                        completedRules += rule
                    }
                }
            }
        } else {
            // Получаем аргументы
            val arg0 = arg(0)
            val arg1 = arg(1)

            // Получаем имя переменной
            val varName = JenaUtil.genVal((arg0 as Literal).value)

            // Компилируем аргументы
            val compiledArg0 = arg0.compile()
            val compiledArg1 = arg1.compile()

            // Передаем завершенные правила дальше
            completedRules += compiledArg0.rules +
                    compiledArg1.rules

            // Для всех результатов компиляции
            compiledArg0.heads.forEach { head0 ->
                compiledArg1.heads.forEach { head1 ->
                    // Собираем правила для аргументов
                    val head = head0 + head1

                    // Заполняем шаблон
                    var rule = DECISION_TREE_VAR_ASSIGN_PATTERN
                    rule = rule.replace("<tmp0>", NamingManager.genVarName())
                    rule = rule.replace("<dropped>", NamingManager.genPredicateName())

                    rule = rule.replace("<ruleHead>", head)
                    rule = rule.replace("<newObjHead>", head1)
                    rule = rule.replace("<newObj>", compiledArg1.value)
                    rule = rule.replace("<oldObj>", compiledArg0.value)
                    rule = rule.replace("<varPredicate>", JenaUtil.genLink(POAS_PREF, DECISION_TREE_VAR_PREDICATE))
                    rule = rule.replace("<varName>", varName)

                    // Добавляем в результат
                    completedRules += rule
                }
            }
        }

        return CompilationResult(rules = completedRules)
    }

    override fun clone(): Operator {
        val newArgs = ArrayList<Operator>()

        args.forEach { arg ->
            newArgs.add(arg.clone())
        }

        return Assign(newArgs)
    }

    override fun clone(newArgs: List<Operator>): Operator {
        return Assign(newArgs)
    }

    companion object {

        /**
         * Шаблон правила присваивания значения свойству
         */
        private val PROPERTY_ASSIGN_PATTERN = """
            [
            (<tmp0> <propName> <tmp1>)
            <ruleHead>
            equal(<subjName>, <tmp0>)
            ->
            drop(0)
            (<subjName> <dropped> "true"^^${JenaUtil.XSD_PREF}boolean)
            ]
            
            $PAUSE_MARK
            
            [
            (<subjName> <dropped> <tmp0>)
            <propHead>
            <valueHead>
            ->
            (<subjName> <propName> <value>)
            ]
            
            [
            noValue(<tmp0>, <dropped>)
            <ruleHead>
            equal(<subjName>, <tmp0>)
            ->
            (<subjName> <propName> <value>)
            ]
        """.trimIndent()

        /**
         * Шаблон правила присваивания значения переменной дерева мысли
         */
        private val DECISION_TREE_VAR_ASSIGN_PATTERN = """
            [
            <ruleHead>
            ->
            drop(0)
            (<oldObj> <dropped> "true"^^${JenaUtil.XSD_PREF}boolean)
            ]
            
            $PAUSE_MARK
            
            [
            (<oldObj> <dropped> <tmp0>)
            <newObjHead>
            ->
            (<newObj> <varPredicate> <varName>)
            ]
            
            [
            noValue(<tmp0>, <dropped>)
            <newObjHead>
            ->
            (<newObj> <varPredicate> <varName>)
            ]
        """.trimIndent()
    }
}