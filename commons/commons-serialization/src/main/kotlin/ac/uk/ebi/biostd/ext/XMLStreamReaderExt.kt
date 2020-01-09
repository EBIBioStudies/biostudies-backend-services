package ac.uk.ebi.biostd.ext

import javax.xml.stream.XMLStreamConstants
import javax.xml.stream.XMLStreamReader

/**
 * Executes the given function over the content of the stream reader and returns the function's returning value. After
 * the process is finished the reader is closed.
 *
 * @param function The function to be applied to the stream reader
 */
fun <T> XMLStreamReader.use(function: (XMLStreamReader) -> T): T {
    try {
        return function(this)
    } finally {
        close()
    }
}

/**
 * Maps the elements contained inside the list with the given name using the given mapper function.
 *
 * @param name The name of the XML element containing the elements to be mapped
 * @param function Function to be used to map the elements in the list
 */
fun <T> XMLStreamReader.map(name: String, function: XMLStreamReader.() -> T): List<T> {
    val elements = mutableListOf<T>()
    forEach(name) { elements.add(function()) }

    return elements
}

/**
 * Finds the elements with the given name and applies the given function to each one.
 *
 * @param name The name of the XML element
 * @param function The function to be applied to each element
 */
@Suppress("LoopWithTooManyJumpStatements")
fun XMLStreamReader.forEach(name: String, function: XMLStreamReader.() -> Unit) {
    require(eventType == XMLStreamConstants.START_ELEMENT && localName == name) { "Expected start object $name" }

    while (hasNext()) {
        if (next() == XMLStreamConstants.END_ELEMENT && localName == name) break
        if (isWhiteSpace) continue
        function()
    }

    require(eventType == XMLStreamConstants.END_ELEMENT && localName == name) { "Expected end object $name" }
}

/**
 * The current element text as a trimmed string
 */
val XMLStreamReader.contentAsString: String get() = elementText.trim()
