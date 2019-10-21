package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.ProjectAccessTagAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectMissingAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

class ProjectValidator(private val userPrivilegesService: IUserPrivilegesService) : IProjectValidator {
    override fun validate(project: ExtendedSubmission, context: PersistenceContext) {
        validatePrivileges(project)
        validateProject(project, context)
    }

    private fun validatePrivileges(project: ExtendedSubmission) {
        require(userPrivilegesService.canSubmitProjects(project.user.email)) {
            throw UserCanNotSubmitProjectsException(project.user)
        }
    }

    private fun validateProject(project: ExtendedSubmission, context: PersistenceContext) {
        require(project.accNoTemplate.isNotBlank()) { throw ProjectMissingAccNoPatternException() }
        require(context.isNew(project.accNo)) { throw ProjectAlreadyExistingException(project.accNo) }
        require(context.accessTagExists(project.accNo).not()) {
            throw ProjectAccessTagAlreadyExistingException(project.accNo)
        }
    }
}
