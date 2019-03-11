package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.asString
import java.io.InputStream

data class ResourceFile(val name: String, val inputStream: InputStream, val size: Long) {
    var text: String = ""

    init {
        text = inputStream.asString()
    }
}
