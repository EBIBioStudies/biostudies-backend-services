package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import java.time.ZoneOffset.UTC

internal const val FILES_DIR = "Files"

class ToExtSubmissionMapper(
    private val fileListDocFileRepository: FileListDocFileRepository,
) {
    internal fun toExtSubmission(sub: DocSubmission, includeFileListFiles: Boolean): ExtSubmission =
        ExtSubmission(
            accNo = sub.accNo,
            owner = sub.owner,
            submitter = sub.submitter,
            title = sub.title,
            version = sub.version,
            schemaVersion = sub.schemaVersion,
            method = getMethod(sub.method),
            status = getStatus(sub.status),
            relPath = sub.relPath,
            rootPath = sub.rootPath,
            released = sub.released,
            secretKey = sub.secretKey,
            releaseTime = sub.releaseTime?.atOffset(UTC),
            modificationTime = sub.modificationTime.atOffset(UTC),
            creationTime = sub.creationTime.atOffset(UTC),
            section = sub.section.toExtSection(sub.accNo, sub.version, includeFileListFiles),
            attributes = sub.attributes.toExtAttributes(),
            collections = sub.collections.map { ExtCollection(it.accNo) },
            tags = sub.tags.map { ExtTag(it.name, it.value) },
            stats = sub.stats.map { it.toExtStat() },
            pageTabFiles = sub.pageTabFiles.map { it.toExtFile() },
            storageMode = sub.storageMode
        )

    private fun DocSection.toExtSection(
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean
    ): ExtSection = ExtSection(
        accNo = accNo,
        type = type,
        fileList = fileList?.toExtFileList(subAccNo, subVersion, includeFileListFiles),
        attributes = attributes.toExtAttributes(),
        sections = sections.map { it.toExtSections(subAccNo, subVersion, includeFileListFiles) },
        files = files.map { it.toExtFiles() },
        links = links.map { it.toExtLinks() }
    )

    private fun Either<DocSection, DocSectionTable>.toExtSections(
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean
    ): Either<ExtSection, ExtSectionTable> {
        return bimap(
            { it.toExtSection(subAccNo, subVersion, includeFileListFiles) },
            { it.toExtSectionTable() }
        )
    }

    private fun getStatus(status: DocProcessingStatus) = when (status) {
        DocProcessingStatus.PROCESSED -> ExtProcessingStatus.PROCESSED
        DocProcessingStatus.PROCESSING -> ExtProcessingStatus.PROCESSING
        DocProcessingStatus.REQUESTED -> ExtProcessingStatus.REQUESTED
    }

    private fun getMethod(method: DocSubmissionMethod) = when (method) {
        DocSubmissionMethod.FILE -> ExtSubmissionMethod.FILE
        DocSubmissionMethod.PAGE_TAB -> ExtSubmissionMethod.PAGE_TAB
        DocSubmissionMethod.UNKNOWN -> ExtSubmissionMethod.UNKNOWN
    }

    /**
     * Maps a DocFileList to corresponding Ext type. Note that empty list is used as files as list files are not loaded
     * as part of the submission.
     */
    private fun DocFileList.toExtFileList(
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean
    ): ExtFileList {
        val extFiles = if (includeFileListFiles) fileListFiles(subAccNo, subVersion, fileName) else emptyList()
        return ExtFileList(fileName, extFiles, pageTabFiles = pageTabFiles.map { it.toExtFile() })
    }

    private fun fileListFiles(subAccNo: String, subVersion: Int, fileListName: String): List<ExtFile> {
        val fileName = fileListName.substringAfterLast("/")
        return fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(subAccNo, subVersion, fileName)
            .map { it.file.toExtFile() }
    }
}
