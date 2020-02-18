package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.service.ProjectValidationService
import ac.uk.ebi.biostd.submission.service.TimesService
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.util.date.isBeforeOrEqual
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset

open class ProjectSubmitter(
    private val timesService: TimesService,
    private val accNoPatternUtil: AccNoPatternUtil,
    private val persistenceContext: PersistenceContext,
    private val projectValidationService: ProjectValidationService
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(project: ExtendedSubmission): Submission =
        runCatching { process(project) }.fold(
            { persist(project) },
            { throw InvalidSubmissionException("Submission validation errors", listOf(it)) })

    private fun process(project: ExtendedSubmission): ExtendedSubmission {
        projectValidationService.validate(project)

        val releaseTime = project.releaseDate?.let { parseDate(it) }
        if (releaseTime?.isBeforeOrEqual(OffsetDateTime.now()).orFalse()) {
            project.released = true
            project.addAccessTag(SubFields.PUBLIC_ACCESS_TAG.value)
        }

        project.addAccessTag(project.accNo)
        project.releaseTime = releaseTime

        return project
    }

    private fun persist(project: ExtendedSubmission): Submission {
        val sequencePrefix = accNoPatternUtil.getPattern(project.accNoTemplate!!)
        persistenceContext.createAccNoPatternSequence(sequencePrefix)
        persistenceContext.saveAccessTag(project.accNo)
        project.processingStatus = PROCESSED

        return persistenceContext.saveSubmission(project)
    }

    private fun parseDate(date: String): OffsetDateTime =
        runCatching { LocalDate.parse(date) }
            .recoverCatching { Instant.parse(date).atZone(ZoneId.systemDefault()).toLocalDate() }
            .fold({ it.atStartOfDay().atOffset(ZoneOffset.UTC) }, { throw InvalidDateFormatException(date) })
}
