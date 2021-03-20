package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SECTION_RESERVED_ATTRIBUTES

internal const val TO_EXT_SECTION_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtSectionKt"

fun Section.toExtSection(source: FilesSource): ExtSection = ExtSection(
    type = type,
    accNo = accNo,
    fileList = fileList?.toExtFileList(source),
    attributes = attributes.filterNot { SECTION_RESERVED_ATTRIBUTES.contains(it.name) }.map { it.toExtAttribute() },
    files = files.map { either -> either.bimap({ it.toExtFile(source) }, { it.toExtTable(source) }) },
    links = links.map { either -> either.bimap({ it.toExtLink() }, { it.toExtTable() }) },
    sections = sections.map { either -> either.bimap({ it.toExtSection(source) }, { it.toExtTable(source) }) }
)
