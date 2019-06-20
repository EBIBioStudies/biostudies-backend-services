package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.Section
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.utils.FilesSource

internal fun Section.toExtSection(filesSource: FilesSource): ExtSection {
    return ExtSection(
        accNo = accNo,
        type = type,
        libraryFile = libraryFile?.let { it.toDbLibraryFile(filesSource) },
        attributes = attributes.map { it.toExtAttribute() },
        sections = sections.toExtSection(filesSource),
        files = files.toExtFiles(filesSource),
        links = links.toExtLinks())
}
