package ac.uk.ebi.biostd.ext

import javax.xml.stream.XMLStreamReader

fun XMLStreamReader.forEach(function: () -> Unit) {
    while (hasNext()) {
        try {
            function()
        } catch (exception: NoSuchElementException) {
            break
        }
    }
}
