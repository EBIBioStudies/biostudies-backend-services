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

internal class ToExtSubmissionMapper {
    internal fun toExtSubmission(submission: DocSubmission): ExtSubmission = ExtSubmission(
        accNo = submission.accNo,
        owner = submission.owner,
        submitter = submission.submitter,
        title = submission.title,
        version = submission.version,
        method = getMethod(submission.method),
        status = getStatus(submission.status),
        relPath = submission.relPath,
        rootPath = submission.rootPath,
        released = submission.released,
        secretKey = submission.secretKey,
        releaseTime = submission.releaseTime?.atOffset(UTC),
        modificationTime = submission.modificationTime.atOffset(UTC),
        creationTime = submission.creationTime.atOffset(UTC),
        section = submission.section.toExtSection(),
        attributes = submission.attributes.map { it.toExtAttribute() },
        collections = submission.collections.map { ExtCollection(it.accNo) },
        tags = submission.tags.map { ExtTag(it.name, it.value) },
        stats = submission.stats.map { it.toExtStat() }
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
