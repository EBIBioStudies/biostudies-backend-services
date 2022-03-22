package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.SECTION_RESERVED_ATTRS

class ToExtSectionMapper(private val toExtFileListMapper: ToExtFileListMapper) {
    fun convert(sec: Section, source: FilesSource): ExtSection = ExtSection(
        type = sec.type,
        accNo = sec.accNo,
        fileList = sec.fileList?.let { toExtFileListMapper.convert(it, source) },
        attributes = sec.attributes.filterNot { SECTION_RESERVED_ATTRS.contains(it.name) }.map { it.toExtAttribute() },
        files = sec.files.map { either -> either.bimap({ it.toExtFile(source) }, { it.toExtTable(source) }) },
        links = sec.links.map { either -> either.bimap({ it.toExtLink() }, { it.toExtTable() }) },
        sections = sec.sections.map { either -> either.bimap({ convert(it, source) }, { toExtTable(it, source) }) }
    )

    private fun toExtTable(sectionsTable: SectionsTable, fileSource: FilesSource): ExtSectionTable =
        ExtSectionTable(sectionsTable.elements.map { convert(it, fileSource) })
}
