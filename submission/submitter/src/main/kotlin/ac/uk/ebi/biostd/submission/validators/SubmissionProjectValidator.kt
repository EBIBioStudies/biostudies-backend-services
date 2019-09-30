package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectException
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext

class SubmissionProjectValidator : SubmissionValidator {
    override fun validate(submission: ExtendedSubmission, context: PersistenceContext) {
        submission.attachTo?.let {
            validateProject(it, context)
        }
    }

    private fun validateProject(project: String, context: PersistenceContext) =
        context.getSubmission(project) ?: throw InvalidProjectException(project)
}
