package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.ProjectAccessTagAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.persistence.PersistenceContext
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

internal const val ACC_NO_TEMPLATE_REQUIRED = "The project accession number pattern is required"
internal const val ACC_NO_TEMPLATE_INVALID = "The given template is invalid. Expected pattern is !{TEMPLATE}"

class ProjectValidator(
    private val accNoPatternUtil: AccNoPatternUtil,
    private val userPrivilegesService: IUserPrivilegesService
) : IProjectValidator {
    override fun validate(project: ExtendedSubmission, context: PersistenceContext) {
        validatePrivileges(project)
        validateAccNoTemplate(project)
        validateProject(project, context)
    }

    private fun validatePrivileges(project: ExtendedSubmission) {
        require(userPrivilegesService.canSubmitProjects(project.user.email)) {
            throw UserCanNotSubmitProjectsException(project.user)
        }
    }

    private fun validateAccNoTemplate(project: ExtendedSubmission) {
        require(project.accNoTemplate.isNotBlank()) {
            throw ProjectInvalidAccNoPatternException(ACC_NO_TEMPLATE_REQUIRED)
        }

        require(accNoPatternUtil.isPattern(project.accNoTemplate!!)) {
            throw ProjectInvalidAccNoPatternException(ACC_NO_TEMPLATE_INVALID)
        }
    }

    private fun validateProject(project: ExtendedSubmission, context: PersistenceContext) {
        require(context.isNew(project.accNo)) { throw ProjectAlreadyExistingException(project.accNo) }
        require(context.accessTagExists(project.accNo).not()) {
            throw ProjectAccessTagAlreadyExistingException(project.accNo)
        }
    }
}
