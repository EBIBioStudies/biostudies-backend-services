package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.SECTION_RESERVED_ATTRS

class ToExtSectionMapper(private val toExtFileListMapper: ToExtFileListMapper) {
    suspend fun convert(
        subAccNo: String,
        version: Int,
        sec: Section,
        source: FileSourcesList,
    ): ExtSection =
        ExtSection(
            type = sec.type,
            accNo = sec.accNo,
            fileList = sec.fileList?.let { toExtFileListMapper.convert(subAccNo, version, it, source) },
            attributes = sec.attributes.toExtAttributes(SECTION_RESERVED_ATTRS),
            files =
                sec.files.map { either ->
                    either.bimap({ source.getExtFile(it.path, it.type, it.attributes) }, { it.toExtTable(source) })
                },
            links = sec.links.map { either -> either.bimap({ it.toExtLink() }, { it.toExtTable() }) },
            sections =
                sec.sections.map { either ->
                    either.bimap(
                        { convert(subAccNo, version, it, source) },
                        { toExtTable(subAccNo, version, it, source) },
                    )
                },
        )

    private suspend fun toExtTable(
        subAccNo: String,
        version: Int,
        sectionsTable: SectionsTable,
        fileSource: FileSourcesList,
    ): ExtSectionTable = ExtSectionTable(sectionsTable.elements.map { convert(subAccNo, version, it, fileSource) })
}
