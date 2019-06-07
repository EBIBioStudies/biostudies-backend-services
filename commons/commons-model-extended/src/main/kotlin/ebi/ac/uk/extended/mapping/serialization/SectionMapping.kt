package ebi.ac.uk.extended.mapping.serialization

import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.extensions.libraryFile

class SectionMapper(
    private val attributeMapper: AttributeMapper) {

    internal fun toExtSection(section: Section, fileSource: FilesSource): ExtSection = section.run {
        ExtSection(
            type = type,
            accNo = accNo,
            libraryFile = getLibraryFile(section.libraryFile),
            attributes = attributeMapper.toAttributes(attributes))
    }

    private fun getLibraryFile(fileSource: FilesSource): ExtLibraryFile {
        fileSource.exist(fileSource)
    }
}

