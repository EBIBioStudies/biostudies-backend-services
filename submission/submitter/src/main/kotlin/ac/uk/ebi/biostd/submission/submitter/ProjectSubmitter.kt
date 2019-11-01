package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.processors.IProjectProcessor
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ac.uk.ebi.biostd.submission.validators.IProjectValidator
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.persistence.PersistenceContext
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

open class ProjectSubmitter(
    private val accNoPatternUtil: AccNoPatternUtil,
    private val validators: List<IProjectValidator>,
    private val processors: List<IProjectProcessor>
) {
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    open fun submit(project: ExtendedSubmission, context: PersistenceContext): Submission {
        validators.forEach { validator -> validator.validate(project, context) }
        processors.forEach { processor -> processor.process(project, context) }

        val sequencePrefix = accNoPatternUtil.getPattern(project.accNoTemplate!!)
        context.createAccNoPatternSequence(sequencePrefix)
        context.saveAccessTag(project.accNo)
        context.saveSubmission(project)

        return project
    }
}
