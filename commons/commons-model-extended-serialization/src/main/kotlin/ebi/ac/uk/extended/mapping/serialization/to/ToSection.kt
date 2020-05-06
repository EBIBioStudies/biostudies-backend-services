package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.model.Section

internal const val TO_SECTION_EXTENSIONS = "ebi.ac.uk.extended.mapping.serialization.to.ToSectionKt"

fun ExtSection.toSection(): Section {
    return Section(
        type = type,
        accNo = accNo,
        fileList = fileList?.toFileList(),
        attributes = attributes.mapTo(mutableListOf()) { it.toAttribute() },
        files = files.mapTo(mutableListOf()) { either -> either.bimap({ it.toFile() }, { it.toTable() }) },
        links = links.mapTo(mutableListOf()) { either -> either.bimap({ it.toLink() }, { it.toTable() }) },
        sections = sections.mapTo(mutableListOf()) { either -> either.bimap({ it.toSection() }, { it.toTable() }) })
}
