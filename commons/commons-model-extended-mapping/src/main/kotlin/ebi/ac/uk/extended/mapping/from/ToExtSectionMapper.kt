package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.base.biMap
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SECTION_RESERVED_ATTRS

class ToExtSectionMapper(
    private val toExtFileListMapper: ToExtFileListMapper,
) {
    suspend fun convert(
        accNo: String,
        version: Int,
        sec: ExtSection,
        source: FileSourcesList,
    ): ExtSection =
        sec.copy(
            fileList = sec.fileList?.let { toExtFileListMapper.convert(accNo, version, it, source) },
            files =
                sec.files
                    .biMap(
                        { source.getExtFile(it) },
                        { ExtFileTable(it.files.map { source.getExtFile(it) }) },
                    ),
            sections =
                sec.sections.biMap(
                    { convert(accNo, version, it, source) },
                    { ExtSectionTable(it.sections.map { convert(accNo, version, it, source) }) },
                ),
        )

    suspend fun convert(
        accNo: String,
        version: Int,
        sec: Section,
        source: FileSourcesList,
    ): ExtSection =
        ExtSection(
            type = sec.type,
            accNo = sec.accNo,
            fileList = sec.fileList?.let { toExtFileListMapper.convert(accNo, version, it, source) },
            attributes = sec.attributes.toExtAttributes(SECTION_RESERVED_ATTRS),
            files =
                sec.files.biMap(
                    { source.getExtFile(it) },
                    { ExtFileTable(it.elements.map { source.getExtFile(it) }) },
                ),
            links = sec.links.biMap({ it.toExtLink() }, { it.toExtTable() }),
            sections =
                sec.sections.biMap(
                    { convert(accNo, version, it, source) },
                    { ExtSectionTable(it.elements.map { convert(accNo, version, it, source) }) },
                ),
        )
}
