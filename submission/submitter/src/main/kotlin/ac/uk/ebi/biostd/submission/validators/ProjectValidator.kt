package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.ProjectAccessTagAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ebi.ac.uk.base.ifFalse
import ebi.ac.uk.base.ifTrue
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext

class ProjectValidator : IProjectValidator {
    override fun validate(project: ExtendedSubmission, context: PersistenceContext) {
        context.isNew(project.accNo).ifFalse {
            throw ProjectAlreadyExistingException(project.accNo)
        }

        context.accessTagExists(project.accNo).ifTrue {
            throw ProjectAccessTagAlreadyExistingException(project.accNo)
        }
    }
}
