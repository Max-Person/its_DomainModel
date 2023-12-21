package its.model.expressions.xml

import its.model.definition.types.ComparisonType
import its.model.definition.types.EnumValue
import its.model.expressions.Operator
import its.model.expressions.literals.*
import its.model.expressions.operators.*
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.findAnnotations

/**
 * Построение выражения из XML
 * TODO
 */
object ExpressionXMLBuilder {

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
                return VariableLiteral(el.getAttribute("name"))
            }

            "DecisionTreeVar" -> {
                return DecisionTreeVarLiteral(el.getAttribute("name"))
            }

            "Class" -> {
                return ClassLiteral(el.getAttribute("name"))
            }

            "Object" -> {
                return ObjectLiteral(
                    el.getAttribute("name"),
                    el.getAttribute("type"),
                )
            }

            "ComparisonResult" -> {
                return EnumLiteral(EnumValue(ComparisonType.enumName, el.getAttribute("value")))
            }

            "String" -> {
                return StringLiteral(el.getAttribute("value"))
            }

            "Boolean" -> {
                return BooleanLiteral(el.getAttribute("value").toBoolean())
            }

            "Integer" -> {
                return IntegerLiteral(el.getAttribute("value").toInt())
            }

            "Double" -> {
                return DoubleLiteral(el.getAttribute("value").toDouble())
            }

            "Enum" -> {
                return EnumLiteral(
                    EnumValue(
                        el.getAttribute("owner"),
                        el.getAttribute("value"),
                    )
                )
            }

            "AssignToDecisionTreeVar" -> {
                return if (el.hasAttribute("varName"))
                    AssignDecisionTreeVar(
                        el.getAttribute("varName"),
                        children[0],
                    )
                else
                    AssignDecisionTreeVar(
                        (children[0] as DecisionTreeVarLiteral).name,
                        children[1],
                    )
            }

            "AssignToProperty" -> {
                return if (el.hasAttribute("propertyName"))
                    AssignProperty(
                        children[0],
                        el.getAttribute("propertyName"),
                        children[1],
                    )
                else
                    AssignProperty(
                        children[0],
                        (children[1] as StringLiteral).value,
                        children[2],
                    )
            }

            "CheckClass" -> {
                return CheckClass(children[0], children[1])
            }

            "CheckPropertyValue" -> {
                return if (el.hasAttribute("propertyName"))
                    CheckPropertyValue(
                        children[0],
                        el.getAttribute("propertyName"),
                        children[1],
                    )
                else
                    CheckPropertyValue(
                        children[0],
                        (children[1] as StringLiteral).value,
                        children[2],
                    )
            }

            "CheckRelationship" -> {
                return if (el.hasAttribute("relationshipName"))
                    CheckRelationship(
                        children[0],
                        el.getAttribute("relationshipName"),
                        children.subList(1, children.size),
                    )
                else
                    CheckRelationship(
                        children[1],
                        (children[0] as StringLiteral).value,
                        children.subList(2, children.size),
                    )
            }

            "Compare" -> {
                return if (!el.hasAttribute("operator")) {
                    Compare(children[0], children[1])
                } else {
                    val operator = CompareWithComparisonOperator.ComparisonOperator.fromString(
                        el.getAttribute("operator")
                    )!!
                    CompareWithComparisonOperator(children[0], operator, children[1])
                }
            }

            "ExistenceQuantifier" -> {
                val selector = children[0]
                val condition = children[1]
                val varName = el.getAttribute("varName")
                val type = if (el.hasAttribute("type")) el.getAttribute("type")
                else getTypeFromConditionExpr(selector, varName, el.tagName)
                return ExistenceQuantifier(TypedVariable(type, varName), selector, condition)
            }

            "ForAllQuantifier" -> {
                val selector = children[0]
                val condition = children[1]
                val varName = el.getAttribute("varName")
                val type = if (el.hasAttribute("type")) el.getAttribute("type")
                else getTypeFromConditionExpr(selector, varName, el.tagName)
                return ForAllQuantifier(TypedVariable(type, varName), selector, condition)
            }

            "GetByCondition" -> {
                val condition = children[0]
                val varName = el.getAttribute("varName")
                val type = if (el.hasAttribute("type")) el.getAttribute("type")
                else getTypeFromConditionExpr(condition, varName, el.tagName)
                return GetByCondition(TypedVariable(type, varName), condition)
            }

            "GetByRelationship" -> {
                val relationshipName = if (el.hasAttribute("relationshipName")) el.getAttribute("relationshipName")
                else (children[1] as StringLiteral).value
                return GetByRelationship(children[0], relationshipName)
            }

            "GetClass" -> {
                return GetClass(children[0])
            }

            "GetExtreme" -> {
                val condition = children[1]
                val extremeCondition = children[0]
                val varName = el.getAttribute("varName")
                val extremeVarName = el.getAttribute("extremeVarName")
                val type = if (el.hasAttribute("type")) el.getAttribute("type")
                else getTypeFromConditionExpr(condition, varName, el.tagName)
                return GetExtreme(type, varName, condition, extremeVarName, extremeCondition)
            }

            "GetPropertyValue" -> {
                val propertyName = if (el.hasAttribute("propertyName")) el.getAttribute("propertyName")
                else (children[1] as StringLiteral).value
                return GetPropertyValue(children[0], propertyName)
            }

            "LogicalAnd" -> {
                return LogicalAnd(children[0], children[1])
            }

            "LogicalOr" -> {
                return LogicalOr(children[0], children[1])
            }

            "LogicalNot" -> {
                return LogicalNot(children[0])
            }

            else -> {
                if (el.hasAttribute("name"))
                //В случае неизвестного типа узла собираем имя в строковый литерал
                //Это нужно чтобы данные из устаревших типов операторов (ссылка на свойство и т.п.) не потерялись
                    return StringLiteral(el.getAttribute("name"))
                else
                    throw IllegalArgumentException("Неизвестный тип узла ${el.nodeName}.")
            }
        }
    }
}