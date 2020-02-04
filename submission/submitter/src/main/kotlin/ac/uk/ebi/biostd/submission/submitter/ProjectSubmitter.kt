package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.service.ProjectValidationService
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.model.extensions.addAccessTag
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class ProjectSubmitter(
    private val accNoPatternUtil: AccNoPatternUtil,
    private val persistenceContext: PersistenceContext,
    private val projectValidationService: ProjectValidationService
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(project: ExtendedSubmission): Submission {
        val processingErrors = process(project)
        if (processingErrors.isEmpty()) {
            val sequencePrefix = accNoPatternUtil.getPattern(project.accNoTemplate!!)
            persistenceContext.createAccNoPatternSequence(sequencePrefix)
            persistenceContext.saveAccessTag(project.accNo)
            project.processingStatus = PROCESSED
            return persistenceContext.saveSubmission(project)
        }

        throw InvalidSubmissionException("Submission validation errors", processingErrors)
    }

    private fun process(project: ExtendedSubmission) =
        listOf(
            runCatching { processProject(project) }
        ).mapNotNull { it.exceptionOrNull() }

    private fun processProject(project: ExtendedSubmission): ExtendedSubmission {
        projectValidationService.validate(project)
        project.addAccessTag(project.accNo)

        return project
    }
}
