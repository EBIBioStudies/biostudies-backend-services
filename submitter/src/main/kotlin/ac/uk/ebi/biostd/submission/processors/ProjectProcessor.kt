package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectException
import arrow.core.getOrElse
import ebi.ac.uk.base.applyIfNotBlank
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext

class ProjectProcessor : SubmissionProcessor {
    override fun process(submission: ExtendedSubmission, persistenceContext: PersistenceContext) {
        submission.attachTo.applyIfNotBlank { project ->
            persistenceContext.getSubmission(project).getOrElse { throw InvalidProjectException(project) } }
    }
}
