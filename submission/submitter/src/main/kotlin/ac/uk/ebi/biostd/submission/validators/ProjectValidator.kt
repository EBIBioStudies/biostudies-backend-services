package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.ProjectAccessTagAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ebi.ac.uk.base.ifTrue
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

class ProjectValidator : SubmissionValidator {
    override fun validate(submission: ExtendedSubmission, context: PersistenceContext) {
        context.isNew(submission.accNo).not().ifTrue {
            throw ProjectAlreadyExistingException(submission.accNo)
        }

        context.accessTagExists(submission.accNo).ifTrue {
            throw ProjectAccessTagAlreadyExistingException(submission.accNo)
        }
    }
}
