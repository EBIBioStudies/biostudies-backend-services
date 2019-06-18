package ac.uk.ebi.biostd.persistence.mapping.ext.extensions

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ac.uk.ebi.biostd.persistence.model.Submission
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.functions.secondsToInstant
import java.time.ZoneOffset

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



