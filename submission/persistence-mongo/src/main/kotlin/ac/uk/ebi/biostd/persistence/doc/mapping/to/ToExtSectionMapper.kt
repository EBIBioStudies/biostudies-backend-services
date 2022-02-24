package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable

class ToExtSectionMapper(private val toExtFileListMapper: ToExtFileListMapper) {
    fun toExtSection(
        docSection: DocSection,
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean,
    ): ExtSection = ExtSection(accNo = docSection.accNo,
        type = docSection.type,
        fileList = docSection.fileList?.let {
            toExtFileListMapper.toExtFileList(it, subAccNo, subVersion, includeFileListFiles)
        },
        attributes = docSection.attributes.toExtAttributes(),
        sections = docSection.sections.map { it.toExtSections(subAccNo, subVersion, includeFileListFiles) },
        files = docSection.files.map { it.toExtFiles() },
        links = docSection.links.map { it.toExtLinks() })

    private fun Either<DocSection, DocSectionTable>.toExtSections(
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean,
    ): Either<ExtSection, ExtSectionTable> {
        return bimap({ toExtSection(it, subAccNo, subVersion, includeFileListFiles) }, { it.toExtSectionTable() })
    }
}
