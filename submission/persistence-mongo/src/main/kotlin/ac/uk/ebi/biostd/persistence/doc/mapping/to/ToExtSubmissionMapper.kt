package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.Project
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import java.nio.file.Path
import java.time.ZoneOffset.UTC

internal const val FILES_DIR = "Files"
private const val USER_PREFIX = "u"

internal class ToExtSubmissionMapper(private val submissionsPath: Path) {
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
        section = submission.section.toExtSection(getSubmissionSource(submission)),
        attributes = submission.attributes.map { it.toExtAttribute() },
        projects = submission.projects.map { Project(it.accNo) },
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

    private fun getSubmissionSource(dbSubmission: DocSubmission): FilesSource {
        val filesPath = submissionsPath.resolve(dbSubmission.relPath).resolve(FILES_DIR)
        return ComposedFileSource(submissionSources(filesPath))
    }

    private fun submissionSources(filesPath: Path) = listOf(
        PathFilesSource(filesPath),
        PathFilesSource(filesPath.resolve(USER_PREFIX))
    )
}
