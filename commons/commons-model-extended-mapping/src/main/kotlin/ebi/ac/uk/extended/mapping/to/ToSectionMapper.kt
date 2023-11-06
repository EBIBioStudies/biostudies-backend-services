package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable

class ToSectionMapper(private val toFileListMapper: ToFileListMapper) {
    fun convert(sec: ExtSection, calculateDirectories: Boolean = true): Section = Section(
        type = sec.type,
        accNo = sec.accNo,
        fileList = sec.fileList?.let { toFileListMapper.convert(it) },
        attributes = sec.attributes.mapTo(mutableListOf()) { it.toAttribute() },
        files = sec.files.mapTo(mutableListOf()) {
            either ->
            either.bimap({ it.toFile(calculateDirectories) }, { it.toTable(calculateDirectories) })
        },
        links = sec.links.mapTo(mutableListOf()) { either -> either.bimap({ it.toLink() }, { it.toTable() }) },
        sections = sec.sections.mapTo(mutableListOf()) { either -> either.bimap({ convert(it) }, { toTable(it) }) }
    )

    private suspend fun toTable(extSectionTable: ExtSectionTable): SectionsTable =
        SectionsTable(extSectionTable.sections.map { section -> convert(section) })
}
