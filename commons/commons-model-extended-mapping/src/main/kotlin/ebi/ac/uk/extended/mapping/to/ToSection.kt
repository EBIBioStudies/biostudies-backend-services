package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable

internal const val TO_SECTION_EXTENSIONS = "ebi.ac.uk.extended.mapping.to.ToSectionKt"

class ToSection(private val toFileList: ToFileList) {
    fun convert(sec: ExtSection): Section = Section(
        type = sec.type,
        accNo = sec.accNo,
        fileList = sec.fileList?.let { toFileList.convert(it) },
        attributes = sec.attributes.mapTo(mutableListOf()) { it.toAttribute() },
        files = sec.files.mapTo(mutableListOf()) { either -> either.bimap({ it.toFile() }, { it.toTable() }) },
        links = sec.links.mapTo(mutableListOf()) { either -> either.bimap({ it.toLink() }, { it.toTable() }) },
        sections = sec.sections.mapTo(mutableListOf()) { either -> either.bimap({ convert(it) }, { toTable(it) }) }
    )

    fun toTable(extSectionTable: ExtSectionTable): SectionsTable =
        SectionsTable(extSectionTable.sections.map { section -> convert(section) })
}
