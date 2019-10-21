package ac.uk.ebi.biostd.submission.submitter

import ac.uk.ebi.biostd.submission.processors.IProjectProcessor
import ac.uk.ebi.biostd.submission.validators.IProjectValidator
import ebi.ac.uk.model.AccPattern
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.persistence.PersistenceContext

class ProjectSubmitter(
    private val validators: List<IProjectValidator>,
    private val processors: List<IProjectProcessor>
) {
    fun submit(project: ExtendedSubmission, context: PersistenceContext): Submission {
        validators.forEach { validator -> validator.validate(project, context) }
        processors.forEach { processor -> processor.process(project, context) }

        context.createAccNoPatternSequence(AccPattern(project.accNoTemplate!!))
        context.saveAccessTag(project.accNo)
        context.saveSubmission(project)

        return project
    }
}
