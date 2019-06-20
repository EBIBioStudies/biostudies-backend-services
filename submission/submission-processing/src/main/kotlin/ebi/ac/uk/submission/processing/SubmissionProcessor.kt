package ebi.ac.uk.submission.processing

import ac.uk.ebi.biostd.persistence.integration.SubmissionService
import ebi.ac.uk.extended.mapping.serialization.from.toExtAttribute
import ebi.ac.uk.extended.mapping.serialization.from.toExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.submission.processing.accno.getAccNo
import ebi.ac.uk.submission.processing.relpath.getRelPath
import ebi.ac.uk.submission.processing.security.newSecurityKey
import ebi.ac.uk.submission.processing.submission.getProjectTags
import ebi.ac.uk.submission.processing.times.getDates
import ebi.ac.uk.utils.FilesSource
import java.time.OffsetDateTime

class SubmissionProcessor(
    private val submissionService: SubmissionService
) {

    fun processSubmission(submission: Submission, user: User, fileSource: FilesSource): ExtSubmission {
        val accNo = submissionService.getAccNo(submission, user)
        val (createTime, modificationTime, releaseTime) = submissionService.getDates(submission)
        val accessTags = submissionService.getProjectTags(user, submission.attachTo)
        return ExtSubmission(
            accNo = accNo.toString(),
            title = submission.title,
            relPath = getRelPath(accNo),
            rootPath = submission.rootPath,
            released = releaseTime.isBefore(OffsetDateTime.now()),
            secretKey = newSecurityKey(),
            creationTime = createTime,
            releaseTime = releaseTime,
            modificationTime = modificationTime,
            attributes = submission.attributes.map { it.toExtAttribute() },
            accessTags = accessTags,
            tags = submission.tags,
            section = submission.section.toExtSection(fileSource))
    }
}
