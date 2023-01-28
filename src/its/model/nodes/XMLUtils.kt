package its.model.nodes

import org.w3c.dom.Element
import org.w3c.dom.Node

internal fun Element.getChildren() : List<Element>{
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

internal fun Element.getChild() : Element {
    return getChildren().first()
}

internal fun Element.getChildren(tagName : String) : List<Element>{
    return getChildren().filter { it.tagName.equals(tagName) }
}

internal fun Element.getChild(tagName : String) : Element {
    return getChildren(tagName).first()
}

internal fun Element.getByOutcome(outcomeVal : String) : Element?{
    return getChildren("Outcome").firstOrNull { it.getAttribute("value").equals(outcomeVal)}?.getChild()
}

internal fun Element.getSeveralByWrapper(wrapper : String) : List<Element>{
    return getChild(wrapper).getChildren()
}

internal fun Element.getSingleByWrapper(wrapper : String) : Element {
    return getChild(wrapper).getChild()
}