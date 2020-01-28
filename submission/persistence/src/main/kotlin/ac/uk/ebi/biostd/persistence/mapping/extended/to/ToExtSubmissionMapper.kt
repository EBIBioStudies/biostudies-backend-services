package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.Submission
import ebi.ac.uk.extended.model.ExtAccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.functions.secondsToInstant
import ebi.ac.uk.io.sources.PathFilesSource
import java.nio.file.Path
import java.time.ZoneOffset

internal const val TO_EXT_SUBMISSION_EXTENSIONS = "ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionKt"

class ToExtSubmissionMapper(private val submissionsPath: Path) {
    internal fun toExtSubmission(dbSubmission: Submission): ExtSubmission {
        val submissionFileSource = PathFilesSource(submissionsPath.resolve(dbSubmission.relPath))
        return ExtSubmission(
            accNo = dbSubmission.accNo,
            title = dbSubmission.title,
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
