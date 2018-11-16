package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectException
import arrow.core.getOrElse
import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext

class ProjectValidator : SubmissionValidator {
    override fun validate(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        submission.attachTo.applyIfNotBlank { project ->
            persistenceContext.getSubmission(project).getOrElse { throw InvalidProjectException(project) } }
    }
}
