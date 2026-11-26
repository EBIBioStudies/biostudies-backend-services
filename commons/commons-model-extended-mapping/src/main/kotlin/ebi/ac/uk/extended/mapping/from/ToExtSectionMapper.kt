package ebi.ac.uk.extended.mapping.from

import uk.ac.ebi.biostd.base.biMap
import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSectionTable
import uk.ac.ebi.biostd.io.sources.FileSourcesList
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.constants.SECTION_RESERVED_ATTRS

class ToExtSectionMapper(
    private val toExtFileListMapper: ToExtFileListMapper,
    private val toExtLinkListMapper: ToExtLinkListMapper,
) {
    suspend fun convert(
        accNo: String,
        version: Int,
        sec: ExtSection,
        source: FileSourcesList,
    ): ExtSection =
        sec.copy(
            linkList = sec.linkList?.let { toExtLinkListMapper.convert(accNo, version, it) },
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
            linkList = sec.linkList?.let { toExtLinkListMapper.convert(accNo, version, it) },
            fileList = sec.fileList?.let { toExtFileListMapper.convert(accNo, version, it, source) },
            attributes = sec.attributes.toExtAttributes(SECTION_RESERVED_ATTRS),
            files =
                sec.files.biMap(
                    { source.getExtFile(it) },
                    { filesTable -> ExtFileTable(filesTable.elements.map { source.getExtFile(it) }) },
                ),
            links = sec.links.biMap({ it.toExtLink() }, { it.toExtTable() }),
            sections =
                sec.sections.biMap(
                    { convert(accNo, version, it, source) },
                    { ExtSectionTable(it.elements.map { convert(accNo, version, it, source) }) },
                ),
        )
}
