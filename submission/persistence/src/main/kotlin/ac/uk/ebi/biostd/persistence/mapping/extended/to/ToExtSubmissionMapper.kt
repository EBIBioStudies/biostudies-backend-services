package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import java.nio.file.Path

private const val FILES_DIR = "Files"

// Added for background compatibility of old submitter applications
private const val USER_PREFIX = "u"

class ToExtSubmissionMapper(private val submissionsPath: Path) {
    internal fun toExtSubmission(dbSubmission: SubmissionDb): ExtSubmission {
        return ExtSubmission(
            accNo = dbSubmission.accNo,
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
            attributes = dbSubmission.attributes.map { it.toExtAttribute() },
            accessTags = dbSubmission.accessTags.map { ExtAccessTag(it.name) },
            tags = dbSubmission.tags.map { ExtTag(it.classifier, it.name) },
            section = dbSubmission.rootSection.toExtSection(getSubmissionSource(dbSubmission))
        )
    }

    private fun getStatus(status: ProcessingStatus): ebi.ac.uk.extended.model.ExtProcessingStatus {
        return when (status) {
            ProcessingStatus.PROCESSED -> ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
            ProcessingStatus.PROCESSING -> ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSING
        }
    }

    private fun getMethod(method: SubmissionMethod?): ebi.ac.uk.extended.model.ExtSubmissionMethod {
        return when (method) {
            SubmissionMethod.FILE -> ebi.ac.uk.extended.model.ExtSubmissionMethod.FILE
            SubmissionMethod.PAGE_TAB -> ebi.ac.uk.extended.model.ExtSubmissionMethod.PAGE_TAB
            SubmissionMethod.UNKNOWN -> ebi.ac.uk.extended.model.ExtSubmissionMethod.UNKNOWN
            null -> ebi.ac.uk.extended.model.ExtSubmissionMethod.UNKNOWN
        }
    }

    private fun getSubmissionSource(dbSubmission: SubmissionDb): FilesSource {
        val filesPath = submissionsPath.resolve(dbSubmission.relPath).resolve(FILES_DIR)
        return ComposedFileSource(listOf(PathFilesSource(filesPath), PathFilesSource(filesPath.resolve(USER_PREFIX))))
    }
}
