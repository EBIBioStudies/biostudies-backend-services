package ebi.ac.uk.model.builders

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File

class FileBuilder {
    var path: String? = null
    var attributes: List<Attribute> = emptyList()

    fun build(): File {
        return File(requireNotNull(path) { "file path is required" }, attributes = attributes)
    }
}
