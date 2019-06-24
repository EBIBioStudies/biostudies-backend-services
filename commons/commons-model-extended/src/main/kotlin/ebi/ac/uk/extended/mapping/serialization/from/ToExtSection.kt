package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.Section

internal const val TO_EXT_SECTION_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.from.ToExtSectionKt"

fun Section.toExtSection(source: FilesSource): ExtSection {
    return ExtSection(
        type = type,
        accNo = accNo,
        fileList = fileList?.toExtFileList(source),
        attributes = attributes.map { it.toExtAttribute() },
        files = files.map { either -> either.bimap({ it.toExtFile(source) }, { it.toExtTable(source) }) },
        links = links.map { either -> either.bimap({ it.toExtLink() }, { it.toExtTable() }) },
        sections = sections.map { either -> either.bimap({ it.toExtSubSection(source) }, { it.toExtTable(source) }) })
}

fun Section.toExtSubSection(source: FilesSource): ExtSection {
    return ExtSection(
        type = type,
        accNo = accNo,
        fileList = fileList?.toExtFileList(source),
        attributes = attributes.map { it.toExtAttribute() }
    )
}
