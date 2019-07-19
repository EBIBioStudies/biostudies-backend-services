package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Submission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.functions.secondsToInstant
import ebi.ac.uk.io.FilesSource
import java.time.ZoneOffset

internal const val TO_EXT_SUBMISSION_EXTENSIONS = "ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionKt"

internal fun Submission.toExtSubmission(filesSource: FilesSource): ExtSubmission {
    return ExtSubmission(
        accNo = accNo,
        title = title,
        relPath = relPath,
        rootPath = rootPath,
        released = released,
        secretKey = secretKey,
        releaseTime = asOffset(releaseTime),
        modificationTime = asOffset(releaseTime),
        creationTime = asOffset(creationTime),
        attributes = attributes.map { it.toExtAttribute() },
        accessTags = accessTags.map(AccessTag::name),
        tags = tags.map { Pair(it.classifier, it.name) },
        section = rootSection.toExtSection(filesSource)
    )
}

private fun asOffset(seconds: Long) = secondsToInstant(seconds).atOffset(ZoneOffset.UTC)
