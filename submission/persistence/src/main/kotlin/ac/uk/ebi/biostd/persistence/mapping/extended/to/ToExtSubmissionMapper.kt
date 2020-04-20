package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.SubmissionDb
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.functions.secondsToInstant
import ebi.ac.uk.io.sources.PathFilesSource
import java.nio.file.Path
import java.time.ZoneOffset

private const val FILES_DIR = "Files"

class ToExtSubmissionMapper(private val submissionsPath: Path) {
    internal fun toExtSubmission(dbSubmission: SubmissionDb): ExtSubmission {
        val submissionFileSource = PathFilesSource(submissionsPath.resolve(dbSubmission.relPath).resolve(FILES_DIR))
        return ExtSubmission(
            accNo = dbSubmission.accNo,
            title = dbSubmission.title,
            version = dbSubmission.version,
            method = dbSubmission.method!!, // FIX this add default status
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
            section = dbSubmission.rootSection.toExtSection(submissionFileSource)
        )
    }

    private fun asOffset(seconds: Long) = secondsToInstant(seconds).atOffset(ZoneOffset.UTC)
}
