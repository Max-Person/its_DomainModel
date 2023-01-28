package its.model.expressions

import its.model.expressions.literals.*
import its.model.expressions.operators.*
import its.model.expressions.types.ComparisonResult
import its.model.expressions.types.DataType
import its.model.expressions.visitors.OperatorBehaviour
import its.model.expressions.visitors.OperatorVisitor
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

/**
 * Оператор
 */
interface Operator {

    /**
     * Список аргументов
     */
    val args: List<Operator>

    /**
     * Получить аргумент
     * @param index Индекс аргумента
     * @return Аргумент
     */
    fun arg(index: Int) = args[index]

    /**
     * Список типов данных аргументов
     */
    val argsDataTypes: List<List<DataType>>

    /**
     * Является ли количество аргументов бесконечным
     */
    val isArgsCountUnlimited
        get() = false

    /**
     * Тип данных оператора
     */
    val resultDataType: DataType?

    /**
     * Создает копию объекта
     * @return Копия
     */
    fun clone(): Operator

    /**
     * Создает копию объекта с новыми аргументами
     * @param newArgs Новые аргументы
     * @return Копия
     */
    fun clone(newArgs: List<Operator>): Operator

    /**
     * Семантический анализ дерева
     */
    fun semantic(): Operator {
        val result = simplify()
        result.fillVarsTable()
        return result
    }

    /**
     * Упрощает выражение, удаляя из него отрицания
     * @param isNegative Находится ли текущий оператор под отрицанием
     * @return Упрощенное выражение
     */
    private fun simplify(isNegative: Boolean = false): Operator {
        if (isNegative) {
            return when (this) {
                is LogicalNot -> {
                    arg(0).simplify(false)
                }
                is LogicalOr -> {
                    LogicalAnd(listOf(arg(0).simplify(true), arg(1).simplify(true)))
                }
                is LogicalAnd -> {
                    LogicalOr(listOf(arg(0).simplify(true), arg(1).simplify(true)))
                }
                is ForAllQuantifier -> {
                    val newArgs = args.map { arg -> arg.simplify() }

                    val res = clone(newArgs) as ForAllQuantifier
                    res.isNegative = true

                    res
                }
                is ExistenceQuantifier -> {
                    val newArgs = args.map { arg -> arg.simplify() }

                    val res = clone(newArgs) as ExistenceQuantifier
                    res.isNegative = true

                    res
                }
                is CompareWithComparisonOperator -> {
                    val newArgs = args.map { arg -> arg.simplify() }

                    val res = clone(newArgs) as CompareWithComparisonOperator
                    res.isNegative = true

                    res
                }
                is CheckRelationship -> {
                    val newArgs = args.map { arg -> arg.simplify() }

                    val res = clone(newArgs) as CheckRelationship
                    res.isNegative = true

                    res
                }
                is CheckPropertyValue -> {
                    val newArgs = args.map { arg -> arg.simplify() }

                    val res = clone(newArgs) as CheckPropertyValue
                    res.isNegative = true

                    res
                }
                is CheckClass -> {
                    val newArgs = args.map { arg -> arg.simplify() }

                    val res = clone(newArgs) as CheckClass
                    res.isNegative = true

                    res
                }
                is BooleanLiteral -> {
                    BooleanLiteral(!value.toBoolean())
                }
                else -> {
                    throw IllegalStateException("Отрицание типа $resultDataType невозможно.")
                }
            }
        } else {
            return when (this) {
                is LogicalNot -> {
                    arg(0).simplify(true)
                }
                else -> {
                    val newArgs = args.map { arg -> arg.simplify() }
                    clone(newArgs)
                }
            }
        }
    }

    /**
     * Заполняет таблицу переменных
     */
    private fun fillVarsTable() {
        // TODO
    }

    /**
     * Применяет визитор [visitor] и реализовывает полный обход дерева выражений
     * @param visitor применяемый визитор
     * @return информация, возвращаемая визитором при обработке данного узла
     * @see OperatorVisitor
     * @see run
     */
    fun <I> accept(visitor: OperatorVisitor<I>) : I

    /**
     * Применяет поведение [behaviour] к данному узлу
     * @param behaviour применяемое поведение
     * @return информация, возвращаемая поведением при обработке данного узла
     * @see OperatorBehaviour
     * @see accept
     */
    fun <I> use(behaviour: OperatorBehaviour<I>) : I

    companion object _static{

        /**
         * Изменяемая таблица переменных
         *
         * key - Имя переменной
         *
         * val - Список известных классов переменной
         */
        private val mVarsTable: MutableMap<String, MutableList<String>> = HashMap()

        /**
         * Таблица переменных
         *
         * key - Имя переменной
         *
         * val - Список известных классов переменной
         */
        @JvmStatic
        val varsTable: Map<String, List<String>>
            get() = mVarsTable

        /**
         * Создает выражение из строки с XML
         * @param str Строка с XML
         * @return Выражение
         */
        @JvmStatic
        fun fromXMLString(str: String): Operator? {
            try {
                // Создаем DocumentBuilder
                val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                // Создаем DOM документ из строки
                val document = documentBuilder.parse(InputSource(StringReader(str)))

                // Получаем корневой элемент документа
                val xml: Element = document.documentElement

                // Строим дерево
                return build(xml)
            } catch (ex: ParserConfigurationException) {
                ex.printStackTrace()
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: SAXException) {
                ex.printStackTrace()
            }
            return null
        }

        /**
         * Создает выражение из XML файла
         * @param path Путь к файлу
         * @return Выражение
         */
        @JvmStatic
        fun fromXMLFile(path: String): Operator? {
            try {
                // Создаем DocumentBuilder
                val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                // Создаем DOM документ из файла
                val document = documentBuilder.parse(path)

                // Получаем корневой элемент документа
                val xml: Element = document.documentElement

                // Строим дерево
                return build(xml)
            } catch (ex: ParserConfigurationException) {
                ex.printStackTrace()
            } catch (ex: IOException) {
                ex.printStackTrace()
            } catch (ex: SAXException) {
                ex.printStackTrace()
            }
            return null
        }

        /**
         * Создает оператор из узла XML
         * @param el XML узел
         * @return Оператор
         */
        @JvmStatic
        fun build(el: Element): Operator {
            val children = mutableListOf<Operator>()
            for (i in 0 until el.childNodes.length) {
                val child = el.childNodes.item(i)
                if (child.nodeType == Node.ELEMENT_NODE) {
                    children.add(build(child as Element))
                }
            }

            when (el.nodeName) {
                "Variable" -> {
                    return Variable(el.attributes.getNamedItem("name").nodeValue)
                }

                "DecisionTreeVar" -> {
                    return DecisionTreeVarLiteral(el.attributes.getNamedItem("name").nodeValue)
                }

                "Class" -> {
                    return ClassLiteral(el.attributes.getNamedItem("name").nodeValue)
                }

                "Object" -> {
                    return ObjectLiteral(el.attributes.getNamedItem("name").nodeValue)
                }

                "Property" -> {
                    return PropertyLiteral(el.attributes.getNamedItem("name").nodeValue)
                }

                "Relationship" -> {
                    return RelationshipLiteral(el.attributes.getNamedItem("name").nodeValue)
                }

                "ComparisonResult" -> {
                    return ComparisonResultLiteral(ComparisonResult.fromString(el.attributes.getNamedItem("value").nodeValue)!!)
                }

                "String" -> {
                    return StringLiteral(el.attributes.getNamedItem("value").nodeValue)
                }

                "Boolean" -> {
                    return BooleanLiteral(el.attributes.getNamedItem("value").nodeValue.toBoolean())
                }

                "Integer" -> {
                    return IntegerLiteral(el.attributes.getNamedItem("value").nodeValue.toInt())
                }

                "Double" -> {
                    return DoubleLiteral(el.attributes.getNamedItem("value").nodeValue.toDouble())
                }

                "Enum" -> {
                    return EnumLiteral(
                        el.attributes.getNamedItem("value").nodeValue,
                        el.attributes.getNamedItem("owner").nodeValue
                    )
                }

                "AssignToDecisionTreeVar" -> {
                    return Assign(children)
                }

                "AssignToProperty" -> {
                    return Assign(children)
                }

                "CheckClass" -> {
                    return CheckClass(children)
                }

                "CheckPropertyValue" -> {
                    return CheckPropertyValue(children)
                }

                "CheckRelationship" -> {
                    return CheckRelationship(children)
                }

                "Compare" -> {
                    return if (el.attributes.getNamedItem("operator") == null) {
                        Compare(children)
                    } else {
                        val operator =
                            CompareWithComparisonOperator.ComparisonOperator.valueOf(el.attributes.getNamedItem("operator").nodeValue)!!
                        CompareWithComparisonOperator(children, operator)
                    }
                }

                "ExistenceQuantifier" -> {
                    return ExistenceQuantifier(children, el.attributes.getNamedItem("varName").nodeValue)
                }

                "ForAllQuantifier" -> {
                    return ForAllQuantifier(children, el.attributes.getNamedItem("varName").nodeValue)
                }

                "GetByCondition" -> {
                    return GetByCondition(children, el.attributes.getNamedItem("varName").nodeValue)
                }

                "GetByRelationship" -> {
                    return GetByRelationship(
                        children,
                        if (el.attributes.getNamedItem("varName") == null) null
                        else el.attributes.getNamedItem("varName").nodeValue
                    )
                }

                "GetClass" -> {
                    return GetClass(children)
                }

                "GetExtreme" -> {
                    return GetExtreme(
                        children,
                        el.attributes.getNamedItem("varName").nodeValue,
                        el.attributes.getNamedItem("extremeVarName").nodeValue
                    )
                }

                "GetPropertyValue" -> {
                    return GetPropertyValue(children)
                }

                "LogicalAnd" -> {
                    return LogicalAnd(children)
                }

                "LogicalOr" -> {
                    return LogicalOr(children)
                }

                "LogicalNot" -> {
                    return LogicalNot(children)
                }
            }
            throw IllegalArgumentException("Неизвестный тип узла ${el.nodeName}.")
        }
    }
}