package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.exceptions.InvalidSubmissionException
import ac.uk.ebi.biostd.submission.processors.IProjectProcessor
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class ProjectSubmitter(
    private val accNoPatternUtil: AccNoPatternUtil,
    private val processors: List<IProjectProcessor>
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(project: ExtendedSubmission, context: PersistenceContext): Submission {
        val processingErrors = process(project, context)
        if (processingErrors.isEmpty()) {
            val sequencePrefix = accNoPatternUtil.getPattern(project.accNoTemplate!!)
            context.createAccNoPatternSequence(sequencePrefix)
            context.saveAccessTag(project.accNo)
            project.processingStatus = PROCESSED
            return context.saveSubmission(project)
        }

        throw InvalidSubmissionException("Submission validation errors", processingErrors)
    }

    private fun process(submission: ExtendedSubmission, context: PersistenceContext): List<Throwable> {
        return processors
            .map { runCatching { it.process(submission, context) } }
            .mapNotNull { it.exceptionOrNull() }
    }
}
