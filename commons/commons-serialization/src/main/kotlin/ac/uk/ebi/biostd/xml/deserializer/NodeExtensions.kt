package ac.uk.ebi.biostd.xml.deserializer

import org.w3c.dom.Node
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun Node.getSubNodes(): List<Node> {
    val result = mutableListOf<Node>()
    for (i in 0 until childNodes.length) {
        childNodes.item(i).apply {
            if (nodeType == Node.ELEMENT_NODE)
                result.add(this)
        }
    }

    return result
}

fun Node.getNode(key: Any): Node {
    val node = findNode(key)
    require(node != null) { "expected to find array node `$key` in node ${toXmlString()}" }
    return node
}

fun Node.findNode(key: Any): Node? {
    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)
        if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == key.toString())
            return child
    }

    return null
}

fun Node.getSubNodes(type: Any): List<Node> {
    val result = mutableListOf<Node>()
    for (i in 0 until childNodes.length) {
        val child = childNodes.item(i)
        if (child.nodeType == Node.ELEMENT_NODE && child.nodeName == type.toString())
            result.add(child)
    }

    return result
}

fun Node.toXmlString() =
    StringWriter().also { writer ->
        TransformerFactory.newInstance().newTransformer().also { transformed ->
            transformed.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformed.setOutputProperty(OutputKeys.INDENT, "yes")
            transformed.transform(DOMSource(this), StreamResult(writer))
        }
    }.toString()

fun Node.getNodeAttribute(key: Any): String = getNode(key).textContent.trim()
fun Node.findNodeAttribute(key: Any): String? = findNode(key)?.textContent?.trim()

fun Node.findProperty(key: Any): String? = attributes.getNamedItem(key.toString())?.textContent?.trim()
fun Node.getProperty(key: Any): String = attributes.getNamedItem(key.toString()).textContent.trim()
