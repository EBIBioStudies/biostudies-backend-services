package ebi.ac.uk.extended.mapping.serialization

import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.model.File

class FileMapping(private val attributeMapper: AttributeMapper) {

    fun toExtFile(file: File, fileSource: FilesSource): ExtFile =
        ExtFile(file.path, fileSource.get(file.path), attributeMapper.toAttributes(file.attributes))
}
