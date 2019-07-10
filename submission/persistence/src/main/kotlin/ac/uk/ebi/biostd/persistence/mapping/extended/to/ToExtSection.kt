package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.Section
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.io.FilesSource

internal fun Section.toExtSection(filesSource: FilesSource): ExtSection {
    return ExtSection(
        accNo = accNo,
        type = type,
        fileList = fileList?.toExtFileList(filesSource),
        attributes = attributes.map { it.toExtAttribute() },
        sections = sections.toExtSections(filesSource),
        files = files.toExtFiles(filesSource),
        links = links.toExtLinks())
}
