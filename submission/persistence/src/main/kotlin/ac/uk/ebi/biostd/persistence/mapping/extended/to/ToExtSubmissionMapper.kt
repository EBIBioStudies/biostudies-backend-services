package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.SubmissionMethod.UNKNOWN
import java.nio.file.Path

private const val FILES_DIR = "Files"

// Added for background compatibility of old submitter applications
private const val USER_PREFIX = "u"

class ToExtSubmissionMapper(private val submissionsPath: Path) {
    internal fun toExtSubmission(dbSubmission: SubmissionDb): ExtSubmission {
        return ExtSubmission(
            accNo = dbSubmission.accNo,
            title = dbSubmission.title,
            processingStatus = dbSubmission.status,
            version = dbSubmission.version,
            method = dbSubmission.method ?: UNKNOWN,
            status = dbSubmission.status,
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

    private fun getSubmissionSource(dbSubmission: SubmissionDb): FilesSource {
        val filesPath = submissionsPath.resolve(dbSubmission.relPath).resolve(FILES_DIR)
        return ComposedFileSource(listOf(PathFilesSource(filesPath), PathFilesSource(filesPath.resolve(USER_PREFIX))))
    }
}
