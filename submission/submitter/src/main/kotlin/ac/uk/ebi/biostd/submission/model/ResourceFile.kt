package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.asString
import java.io.InputStream

data class ResourceFile(val name: String, val inputStream: InputStream, val size: Long) {
    val text: String
        get() = inputStream.asString()
}
