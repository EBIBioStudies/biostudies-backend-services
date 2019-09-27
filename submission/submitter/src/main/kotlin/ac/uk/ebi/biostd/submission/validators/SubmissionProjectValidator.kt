package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectException
import ac.uk.ebi.biostd.submission.exceptions.MissingProjectAccessTagException
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionProjectValidator : SubmissionValidator {
    override fun validate(submission: ExtendedSubmission, context: PersistenceContext) {
        submission.attachTo?.let {
            validateProject(it, context)
            validateProjectAccessTag(it, submission, context)
        }
    }

    private fun validateProject(project: String, context: PersistenceContext) =
        context.getSubmission(project) ?: throw InvalidProjectException(project)

    private fun validateProjectAccessTag(project: String, submission: ExtendedSubmission, context: PersistenceContext) =
        context.getParentAccessTags(submission)
            .filter { tag -> tag == project }
            .ifEmpty { throw MissingProjectAccessTagException(project) }
}
