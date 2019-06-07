package ebi.ac.uk.extended.mapping.persistence

import ac.uk.ebi.biostd.persistence.model.AccessTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.functions.secondsToInstant
import java.time.ZoneOffset
import ac.uk.ebi.biostd.persistence.model.File as FileDb
import ac.uk.ebi.biostd.persistence.model.Link as LinkDb
import ac.uk.ebi.biostd.persistence.model.Section as SectionDb
import ac.uk.ebi.biostd.persistence.model.Submission as SubmissionDb

class DbExtSectionMapper {

    fun toExtSubmission(submissionDb: SubmissionDb): ExtSubmission = submissionDb.run {
        ExtSubmission(
            accNo = accNo,
            title = title,
            secretKey = secretKey,
            relPath = relPath,
            rootPath = rootPath,
            released = released,
            creationTime = secondsToInstant(creationTime).atOffset(ZoneOffset.UTC),
            modificationTime = secondsToInstant(releaseTime).atOffset(ZoneOffset.UTC),
            releaseTime = secondsToInstant(releaseTime).atOffset(ZoneOffset.UTC),
            attributes = toAttributes(attributes),
            accessTags = accessTags.map(AccessTag::name),
            tags = tags.map { Pair(it.classifier, it.name) },
            section = toExtSection(rootSection))
    }
}
