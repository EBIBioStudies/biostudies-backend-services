package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionStat
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtStat
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.Project
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import java.nio.file.Path

private const val FILES_DIR = "Files"

// Added for background compatibility of old submitter applications
public const val USER_PREFIX = "u"

class ToExtSubmissionMapper(private val submissionsPath: Path) {
    internal fun toExtSubmission(dbToExtRequest: DbToExtRequest): ExtSubmission {
        val (dbSubmission, stats) = dbToExtRequest
        return toExtSubmission(dbSubmission, stats)
    }

    private fun toExtSubmission(dbSubmission: DbSubmission, stats: List<DbSubmissionStat>): ExtSubmission {
        return ExtSubmission(
            accNo = dbSubmission.accNo,
            owner = dbSubmission.owner.email,
            submitter = dbSubmission.submitter.email,
            title = dbSubmission.title,
            version = dbSubmission.version,
            method = getMethod(dbSubmission.method),
            status = getStatus(dbSubmission.status),
            relPath = dbSubmission.relPath,
            rootPath = dbSubmission.rootPath,
            released = dbSubmission.released,
            secretKey = dbSubmission.secretKey,
            releaseTime = dbSubmission.releaseTime,
            modificationTime = dbSubmission.modificationTime,
            creationTime = dbSubmission.creationTime,
            section = dbSubmission.rootSection.toExtSection(getSubmissionSource(dbSubmission)),
            attributes = dbSubmission.attributes.map { it.toExtAttribute() },
            projects = dbSubmission.accessTags.map { Project(it.name) },
            tags = dbSubmission.tags.map { ExtTag(it.classifier, it.name) },
            stats = stats.map { toExtMetric(it) }
        )
    }

    private fun toExtMetric(stat: DbSubmissionStat): ExtStat = ExtStat(stat.type.name, stat.value.toString())

    private fun getStatus(status: ProcessingStatus) =
        when (status) {
            ProcessingStatus.PROCESSED -> ExtProcessingStatus.PROCESSED
            ProcessingStatus.PROCESSING -> ExtProcessingStatus.PROCESSING
            ProcessingStatus.REQUESTED -> ExtProcessingStatus.REQUESTED
        }

    private fun getMethod(method: SubmissionMethod?) =
        when (method) {
            SubmissionMethod.FILE -> ExtSubmissionMethod.FILE
            SubmissionMethod.PAGE_TAB -> ExtSubmissionMethod.PAGE_TAB
            SubmissionMethod.UNKNOWN -> ExtSubmissionMethod.UNKNOWN
            null -> ExtSubmissionMethod.UNKNOWN
        }

    private fun getSubmissionSource(dbSubmission: DbSubmission): FilesSource {
        val filesPath = submissionsPath.resolve(dbSubmission.relPath).resolve(FILES_DIR)
        return ComposedFileSource(listOf(PathFilesSource(filesPath), PathFilesSource(filesPath.resolve(USER_PREFIX))))
    }
}
