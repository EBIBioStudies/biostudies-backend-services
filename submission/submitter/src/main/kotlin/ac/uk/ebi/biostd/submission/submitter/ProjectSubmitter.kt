package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.service.ProjectValidationService
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.model.extensions.addAccessTag
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class ProjectSubmitter(
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
        project.addAccessTag(project.accNo)

        return project
    }

    private fun persist(project: ExtendedSubmission): Submission {
        val sequencePrefix = accNoPatternUtil.getPattern(project.accNoTemplate!!)
        persistenceContext.createAccNoPatternSequence(sequencePrefix)
        persistenceContext.saveAccessTag(project.accNo)
        project.processingStatus = PROCESSED

        return persistenceContext.saveSubmission(project)
    }
}
