package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import java.time.ZoneOffset.UTC

internal const val FILES_DIR = "Files"

class ToExtSubmissionMapper(private val toExtSectionMapper: ToExtSectionMapper) {
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
            section = toExtSectionMapper.toExtSection(sub.section, sub.accNo, sub.version, includeFileListFiles),
            attributes = sub.attributes.toExtAttributes(),
            collections = sub.collections.map { ExtCollection(it.accNo) },
            tags = sub.tags.map { ExtTag(it.name, it.value) },
            stats = sub.stats.map { it.toExtStat() },
            pageTabFiles = sub.pageTabFiles.map { it.toExtFile() },
            storageMode = sub.storageMode
        )

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
}
