package ac.uk.ebi.biostd.ext

import com.fasterxml.jackson.dataformat.xml.XmlMapper
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

inline fun <reified T> XMLStreamReader.mapList(listName: String, mapper: XmlMapper): List<T> {
    val elements: MutableList<T> = mutableListOf()

    next()
    require(name.toString() == listName) { "Given list should start with $listName" }

    while (hasNext()) {
        try {
            elements.add(mapper.readValue(this, T::class.java))
        } catch (exception: NoSuchElementException) {
            break
        }
    }

    return elements.toList()
}
