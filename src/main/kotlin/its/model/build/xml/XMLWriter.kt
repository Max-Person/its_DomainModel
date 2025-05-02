package its.model.build.xml

import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.StringWriter
import java.io.Writer
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Общий код для классов, сериализующих объекты в XML
 */
abstract class XMLWriter protected constructor(protected val document: Document) {

    protected fun newElement(name:String) : Element {
        return document.createElement(name)
    }

    protected fun Element.withAttribute(name: String, value: String) : Element {
        return this.apply { setAttribute(name, value) }
    }

    protected fun Element.withChild(child: Element) : Element {
        return this.apply { appendChild(child) }
    }

    protected fun Element.withText(text:String) : Element {
        return this.apply { appendChild(document.createCDATASection(text)) }
    }

    companion object {

        @JvmStatic
        protected fun writeToXml(writer: Writer, createElement: (document: Document) -> Element) {
            val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val document = db.newDocument()

            document.appendChild(createElement(document))

            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(DOMSource(document), StreamResult(writer));
        }

        @JvmStatic
        protected fun writeToXmlString(createElement: (document: Document) -> Element): String {
            return StringWriter()
                .also { writeToXml(it, createElement) }
                .toString()
        }
    }
}