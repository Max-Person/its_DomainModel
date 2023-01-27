package its.model.nodes

import its.model.visitors.DecisionTreeVisitor
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

sealed class DecisionTreeNode{
    companion object _static{
        /**
         * Создает дерево решений из XML строки
         * @param str Строка с XML
         * @return начальный узел дерева решений
         */
        fun fromXMLString(str: String): StartNode? {
            try {
                // Создаем DocumentBuilder
                val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                // Создаем DOM документ из строки
                val document = documentBuilder.parse(InputSource(StringReader(str)))

                // Получаем корневой элемент документа
                val xml: Element = document.documentElement

                // Строим дерево
                val root = build(xml)
                require(root is StartNode){
                    "Дерево решений должно начинаться со стартового узла"
                }
                return root
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
         * Создает дерево решений из XML файла
         * @param path Путь к файлу
         * @return начальный узел дерева решений
         */
        fun fromXMLFile(path: String): StartNode? {
            try {
                // Создаем DocumentBuilder
                val documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                // Создаем DOM документ из строки
                val document = documentBuilder.parse(path)

                // Получаем корневой элемент документа
                val xml: Element = document.documentElement

                // Строим дерево
                val root = build(xml)
                require(root is StartNode){
                    "Дерево решений должно начинаться со стартового узла"
                }
                return root
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
         * Создает узел дерева решений из узла XML
         * @param el XML узел
         * @return узел дерева решений
         */
        fun build(el: Element?): DecisionTreeNode? {
            if(el == null)
                return null

            return when (el.tagName) {
                "StartNode" -> StartNode(el)
                "BranchResultNode" -> BranchResultNode(el)
                "QuestionNode" -> QuestionNode(el)
                "FindActionNode" -> FindActionNode(el)
                "LogicAggregationNode" -> LogicAggregationNode(el)
                "CycleAggregationNode" -> CycleAggregationNode(el)
                "PredeterminingFactorsNode" -> PredeterminingFactorsNode(el)
                "UndeterminedNode" -> UndeterminedResultNode()
                else -> null
            }
        }
    }

    abstract fun <I> accept(visitor: DecisionTreeVisitor<I>) : I
}

fun Element.getChildren() : List<Element>{
    val out = mutableListOf<Element>()
    var child = this.firstChild
    while (child != null){
        if(child.nodeType == Node.ELEMENT_NODE){
            out.add(child as Element)
        }
        child = child.nextSibling
    }
    return out
}

fun Element.getChild() : Element{
    return getChildren().first()
}

fun Element.getChildren(tagName : String) : List<Element>{
    return getChildren().filter { it.tagName.equals(tagName) }
}

fun Element.getChild(tagName : String) : Element{
    return getChildren(tagName).first()
}

fun Element.getByOutcome(outcomeVal : String) : Element?{
    return getChildren("Outcome").firstOrNull { it.getAttribute("value").equals(outcomeVal)}?.getChild()
}

fun Element.getSeveralByWrapper(wrapper : String) : List<Element>{
    return getChild(wrapper).getChildren()
}

fun Element.getSingleByWrapper(wrapper : String) : Element{
    return getChild(wrapper).getChild()
}