package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable

class ToExtSectionMapper(private val fileListMapper: ToExtFileListMapper) {

    fun toExtSection(
        section: DocSection,
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean,
    ): ExtSection = ExtSection(
        accNo = section.accNo,
        type = section.type,
        fileList = section.fileList?.toExtFileList(subAccNo, subVersion, includeFileListFiles),
        attributes = section.attributes.toExtAttributes(),
        sections = section.sections.map { it.toExtSections(subAccNo, subVersion, includeFileListFiles) },
        files = section.files.map { it.toExtFiles() },
        links = section.links.map { it.toExtLinks() }
    )

    private fun DocFileList.toExtFileList(
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean
    ): ExtFileList = fileListMapper.toExtFileList(this, subAccNo, subVersion, includeFileListFiles)

    private fun Either<DocSection, DocSectionTable>.toExtSections(
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean,
    ): Either<ExtSection, ExtSectionTable> =
        bimap({ toExtSection(it, subAccNo, subVersion, includeFileListFiles) }, { it.toExtSectionTable() })
}