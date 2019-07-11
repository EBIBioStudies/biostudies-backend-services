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
