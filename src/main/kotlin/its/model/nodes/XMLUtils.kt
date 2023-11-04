package its.model.nodes

import its.model.nullCheck
import org.w3c.dom.Element
import org.w3c.dom.Node

internal fun Element.getChildren(): List<Element> {
    val out = mutableListOf<Element>()
    var child = this.firstChild
    while (child != null) {
        if (child.nodeType == Node.ELEMENT_NODE) {
            out.add(child as Element)
        }
        child = child.nextSibling
    }
    return out
}

internal fun Element.getChild(): Element? {
    return getChildren().firstOrNull()
}

internal fun Element.getChildren(tagName: String): List<Element> {
    return getChildren().filter { it.tagName.equals(tagName) }
}

internal fun Element.getChild(tagName: String): Element? {
    return getChildren(tagName).firstOrNull()
}

internal fun Element.getByOutcome(outcomeVal: String): Element? {
    return getOutcome(outcomeVal)?.getChild()
}

internal fun Element.getOutcome(outcomeVal: String): Element? {
    return getChildren("Outcome").firstOrNull { it.getAttribute("value").equals(outcomeVal) }
}

internal fun Element.getSeveralByWrapper(wrapper: String): List<Element> {
    return getChild(wrapper).nullCheck("No wrapper tag '$wrapper' was found inside element $this").getChildren()
}

internal fun Element.getSingleByWrapper(wrapper: String): Element? {
    return getChild(wrapper).nullCheck("No wrapper tag '$wrapper' was found inside element $this").getChild()
}

private const val ADDITIONAL_INFO_PREFIX = "_"

internal fun Element.getAdditionalInfo(): Map<String, String> {
    val info = mutableMapOf<String, String>()
    for (i in 0 until this.attributes.length) {
        val atr = this.attributes.item(i)
        if (atr.nodeName.startsWith(ADDITIONAL_INFO_PREFIX)) {
            info[atr.nodeName.drop(ADDITIONAL_INFO_PREFIX.length)] = atr.nodeValue
        }
    }
    return info
}