package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.model.Section

fun Section.toExtSection(source: FilesSource): ExtSection {
    return ExtSection(
        type = type,
        accNo = accNo,
        libraryFile = libraryFile?.toExtLibraryFile(source),
        attributes = attributes.map { it.toExtAttribute() },
        files = files.map { either -> either.bimap({ it.toExtFile(source) }, { it.toExtTable(source) }) },
        links = links.map { either -> either.bimap({ it.toExtLink() }, { it.toExtTable() }) },
        sections = sections.map { either -> either.bimap({ it.toExtSection(source) }, { it.toExtTable(source) }) })
}
