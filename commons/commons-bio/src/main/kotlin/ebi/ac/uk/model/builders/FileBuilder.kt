package ebi.ac.uk.model.builders

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile

class FileBuilder {
    var path: String = ""
    var attributes: List<Attribute> = emptyList()

    fun build(): BioFile {
        require(path.isNotBlank()) { "File Path is required" }
        return BioFile(path, attributes = attributes)
    }
}
