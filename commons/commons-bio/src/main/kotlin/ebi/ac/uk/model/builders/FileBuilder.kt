package ebi.ac.uk.model.builders

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File

class FileBuilder {
    var path: String = ""
    var attributes: List<Attribute> = emptyList()

    fun build(): File {
        require(path.isNotBlank()) { "File Path is required" }
        return File(path, attributes = attributes)
    }
}
