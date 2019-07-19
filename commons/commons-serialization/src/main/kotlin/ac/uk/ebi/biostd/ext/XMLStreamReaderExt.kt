package ac.uk.ebi.biostd.ext

import javax.xml.stream.XMLStreamReader

/**
 * Iterates over each XML node loaded by the reader and applies the given function .
 *
 * @param function The function to be applied to each element.
 */
fun XMLStreamReader.forEach(function: () -> Unit) {
    while (hasNext()) {
        try {
            function()
        } catch (exception: NoSuchElementException) {
            break
        }
    }
}

inline fun <reified T> XMLStreamReader.mapList(
    listName: String, elementName: String, mapperFunction: (XMLStreamReader) -> T
): List<T> {
    var end = false
    val elements: MutableList<T> = mutableListOf()

    while (hasNext().and(end.not())) {
        next()
        when(eventType) {
            XMLStreamReader.START_ELEMENT -> {
                if (localName == listName) {
                    while (hasNext()) {
                        next()
                        when(eventType) {
                            XMLStreamReader.START_ELEMENT -> {
                                if (localName == elementName) {
                                    elements.add(mapperFunction(this))
                                }
                            }
                        }
                    }
                }
            }
            XMLStreamReader.END_ELEMENT -> end = true
        }
    }

    return elements.toList()
}
