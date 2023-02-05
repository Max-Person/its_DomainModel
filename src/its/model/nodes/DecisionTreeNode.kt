package its.model.nodes

import its.model.nodes.visitors.DecisionTreeBehaviour
import its.model.nodes.visitors.DecisionTreeVisitor
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

sealed class DecisionTreeNode{
    var additionalInfo = mapOf<String, String>()
        private set

    protected fun collectAdditionalInfo(el: Element){
        additionalInfo = el.getAdditionalInfo()
    }

    companion object _static{

        /**
         * Создает дерево решений из XML строки
         * @param str Строка с XML
         * @return начальный узел дерева решений
         */
        @JvmStatic
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
        @JvmStatic
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
         * @see canBuildFrom
         */
        @JvmStatic
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

        /**
         * Может ли узел дерева решений быть построен из узла XML
         * @param el XML узел
         * @return возможность построения узла дерева решений
         */
        @JvmStatic
        fun canBuildFrom(el: Element): Boolean {
            return when (el.tagName) {
                "StartNode" -> true
                "BranchResultNode" -> true
                "QuestionNode" -> true
                "FindActionNode" -> true
                "LogicAggregationNode" -> true
                "CycleAggregationNode" -> true
                "PredeterminingFactorsNode" -> true
                "UndeterminedNode" -> true
                else -> false
            }
        }
    }

    /**
     * Применяет визитор [visitor] и реализовывает полный обход дерева выражений
     * @param visitor применяемый визитор
     * @return информация, возвращаемая визитором при обработке данного узла
     * @see DecisionTreeVisitor
     * @see run
     */
    abstract fun <I> accept(visitor: DecisionTreeVisitor<I>) : I

    /**
     * Применяет поведение [behaviour] к данному узлу
     * @param behaviour применяемое поведение
     * @return информация, возвращаемая поведением при обработке данного узла
     * @see DecisionTreeBehaviour
     * @see accept
     */
    abstract fun <I> use(behaviour: DecisionTreeBehaviour<I>) : I
}