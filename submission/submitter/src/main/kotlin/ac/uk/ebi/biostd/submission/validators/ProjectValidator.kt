package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.ProjectAccessTagAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ebi.ac.uk.base.ifFalse
import ebi.ac.uk.base.ifTrue
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

class ProjectValidator(private val userPrivilegesService: IUserPrivilegesService) : IProjectValidator {
    override fun validate(project: ExtendedSubmission, context: PersistenceContext) {
        validatePrivileges(project)
        validateProject(project, context)
    }

    private fun validatePrivileges(project: ExtendedSubmission) {
        userPrivilegesService.canSubmitProjects(project.user.email).ifFalse {
            throw UserCanNotSubmitProjectsException(project.user)
        }
    }

    private fun validateProject(project: ExtendedSubmission, context: PersistenceContext) {
        context.isNew(project.accNo).ifFalse {
            throw ProjectAlreadyExistingException(project.accNo)
        }

        context.accessTagExists(project.accNo).ifTrue {
            throw ProjectAccessTagAlreadyExistingException(project.accNo)
        }
    }
}
