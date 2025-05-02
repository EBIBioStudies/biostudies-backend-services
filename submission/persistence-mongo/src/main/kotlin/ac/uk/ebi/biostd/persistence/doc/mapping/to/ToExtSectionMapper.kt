package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable

@Suppress("LongParameterList")
class ToExtSectionMapper(
    private val fileListMapper: ToExtFileListMapper,
    private val linkListMapper: ToExtLinkListMapper,
) {
    suspend fun toExtSection(
        section: DocSection,
        subAccNo: String,
        subVersion: Int,
        released: Boolean,
        subRelPath: String,
        includeFileListFiles: Boolean,
    ): ExtSection =
        ExtSection(
            accNo = section.accNo,
            type = section.type,
            fileList =
                section.fileList?.toExtFileList(
                    subAccNo,
                    subVersion,
                    released,
                    subRelPath,
                    includeFileListFiles,
                ),
            linkList = section.linkList?.toExtLinkList(subAccNo, subVersion, released, subRelPath),
            attributes = section.attributes.toExtAttributes(),
            sections =
                section.sections.map {
                    it.toExtSections(
                        subAccNo = subAccNo,
                        subVersion = subVersion,
                        released = released,
                        subRelPath = subRelPath,
                        includeFileListFiles = includeFileListFiles,
                    )
                },
            files = section.files.map { it.toExtFiles(released, subRelPath) },
            links = section.links.map { it.toExtLinks() },
        )

    private suspend fun DocFileList.toExtFileList(
        subAccNo: String,
        subVersion: Int,
        released: Boolean,
        subRelPath: String,
        includeFileListFiles: Boolean,
    ): ExtFileList = fileListMapper.toExtFileList(this, subAccNo, subVersion, released, subRelPath, includeFileListFiles)

    private fun DocLinkList.toExtLinkList(
        subAccNo: String,
        subVersion: Int,
        released: Boolean,
        subRelPath: String,
    ): ExtLinkList = linkListMapper.toExtLinkList(this, subAccNo, subVersion, released, subRelPath)

    private suspend fun Either<DocSection, DocSectionTable>.toExtSections(
        subAccNo: String,
        subVersion: Int,
        released: Boolean,
        subRelPath: String,
        includeFileListFiles: Boolean,
    ): Either<ExtSection, ExtSectionTable> =
        bimap(
            { toExtSection(it, subAccNo, subVersion, released, subRelPath, includeFileListFiles) },
            { it.toExtSectionTable() },
        )
}
