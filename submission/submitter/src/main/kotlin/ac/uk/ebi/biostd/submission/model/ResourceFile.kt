package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.asString
import java.io.InputStream

data class ResourceFile(val name: String, val inputStream: InputStream, val size: Long) {
    var text: String = ""

    // TODO Improve the file reading. This should performed only when necessary
    init {
        text = inputStream.asString()
    }
}
