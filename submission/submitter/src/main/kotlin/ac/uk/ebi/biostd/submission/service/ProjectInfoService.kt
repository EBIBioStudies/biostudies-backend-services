package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.submission.exceptions.ProjectAccNoTemplateAlreadyExistsException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

internal const val ACC_NO_TEMPLATE_REQUIRED = "The AccNoTemplate property is required for projects"
internal const val ACC_NO_TEMPLATE_INVALID = "The given AccNoTemplate is invalid. Expected pattern is !{TEMPLATE}"

class ProjectInfoService(
    private val service: PersistenceService,
    private val queryService: SubmissionMetaQueryService,
    private val accNoUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService
) {
    fun process(request: ProjectRequest): ProjectResponse? {
        val (submitter, subType, template, accNo) = request

        if (subType != "Project") return null

        val isNew = queryService.isNew(accNo)
        require(privilegesService.canSubmitProjects(submitter)) { throw UserCanNotSubmitProjectsException(submitter) }
        validatePattern(template)

        val accNoPattern = accNoUtil.getPattern(template!!)
        validateProject(isNew, accNo, accNoPattern)
        persist(isNew, accNo, accNoPattern)

        return ProjectResponse(request.accNo)
    }

    private fun validateProject(isNew: Boolean, accNo: String, accNoPattern: String) {
        if (isNew && service.accessTagExists(accNo)) throw ProjectAlreadyExistingException(accNo)
        if (isNew && service.sequenceAccNoPatternExists(accNoPattern))
            throw ProjectAccNoTemplateAlreadyExistsException(accNoPattern)
    }

    private fun validatePattern(template: String?) {
        require(template != null) { throw ProjectInvalidAccNoPatternException(ACC_NO_TEMPLATE_REQUIRED) }
        require(accNoUtil.isPattern(template)) { throw ProjectInvalidAccNoPatternException(ACC_NO_TEMPLATE_INVALID) }
    }

    private fun persist(isNew: Boolean, accNo: String, accNoPattern: String) {
        if (isNew) {
            service.saveAccessTag(accNo)
            service.createAccNoPatternSequence(accNoPattern)
        }
    }
}

data class ProjectRequest(val submitter: String, val subType: String, val accNoTemplate: String?, val accNo: String)
data class ProjectResponse(val accessTag: String)
