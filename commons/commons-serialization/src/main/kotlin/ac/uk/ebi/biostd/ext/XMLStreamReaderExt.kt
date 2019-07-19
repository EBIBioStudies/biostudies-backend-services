package ac.uk.ebi.biostd.ext

import javax.xml.stream.XMLStreamReader

/**
 * Maps the elements contained inside the list with the given name using the given mapper function.
 *
 * @param listName The name of the XML element containing the elements to be mapped
 * @param elementName The name of each of the XML element to be mapped
 * @param mapperFunction Function to be used to map the elements in the list
 */
fun <T> XMLStreamReader.mapList(
    listName: String,
    elementName: String,
    mapperFunction: (XMLStreamReader) -> T
): List<T> {
    val elements: MutableList<T> = mutableListOf()

    forEachToken(listName) {
        forEachToken(elementName) { elements.add(mapperFunction(this)) }
    }

    return elements.toList()
}

/**
 * Process the XML with the given name using the given function.
 *
 * @param function The function to be applied to the named element.
 */
fun XMLStreamReader.forEachToken(elementName: String, function: (XMLStreamReader) -> Unit) {
    while (hasNext()) {
        next()
        when (eventType) {
            XMLStreamReader.START_ELEMENT -> {
                if (localName == elementName) {
                    function(this)
                }
            }
        }
    }
}

/**
 * Applies the given function to the reader's current element.
 *
 * @param function The function to be applied to the element.
 */
fun XMLStreamReader.processCurrentElement(function: (XMLStreamReader) -> Unit) {
    var end = false

    while (hasNext().and(end.not())) {
        next()
        when (eventType) {
            XMLStreamReader.START_ELEMENT -> {
                function(this)
            }

            XMLStreamReader.END_ELEMENT -> end = true
        }
    }
}
