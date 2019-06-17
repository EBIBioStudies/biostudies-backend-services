package ebi.ac.uk.extended.processing

import ac.uk.ebi.biostd.persistence.integration.SubmissionService
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.mapping.serialization.AttributeMapper
import ebi.ac.uk.extended.mapping.serialization.SectionMapper
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.processing.accno.AccNoProcessor
import ebi.ac.uk.extended.processing.relpath.RelPathCalculator
import ebi.ac.uk.extended.processing.security.KeysProcessor
import ebi.ac.uk.extended.processing.submission.ProjectProcessor
import ebi.ac.uk.extended.processing.times.TimesProcessor
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import java.time.OffsetDateTime

internal class SubmissionProcessor(
    private val accProcessor: AccNoProcessor,
    private val relPathCalculator: RelPathCalculator,
    private val timesProcessor: TimesProcessor,
    private val keyProcessor: KeysProcessor,
    private val projectProcessor: ProjectProcessor,
    private val attributesMapper: AttributeMapper,
    private val sectionMapper: SectionMapper,
    private val submissionService: SubmissionService
) {

    fun processSubmission(submission: Submission, user: User, fileSource: FilesSource): ExtSubmission {
        val accNo = accProcessor.getAccNo(submission, user)
        val relativePath = relPathCalculator.getRelPath(accNo)
        val (createTime, modificationTime, releaseTime) = timesProcessor.process(submission)
        val secretKey = keyProcessor.newSecurityKey()
        val accessTags = projectProcessor.getProjectTags(user, submission.attachTo)
        val released = releaseTime.isBefore(OffsetDateTime.now())

        val extendedSubmission = ExtSubmission(
            accNo = accNo.toString(),
            title = submission.title,
            relPath = relativePath,
            rootPath = submission.rootPath,
            released = released,
            secretKey = secretKey,
            creationTime = createTime,
            releaseTime = releaseTime,
            modificationTime = modificationTime,
            attributes = attributesMapper.toAttributes(submission.attributes),
            accessTags = accessTags,
            tags = submission.tags,
            section = sectionMapper.toExtSection(submission.section, fileSource))
        return submissionService.saveSubmission(extendedSubmission, user)
    }
}
