package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceContext
import ac.uk.ebi.biostd.persistence.integration.SubmissionQueryService
import ac.uk.ebi.biostd.submission.exceptions.ProjectAccNoTemplateAlreadyExistsException
import ac.uk.ebi.biostd.submission.exceptions.ProjectAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.ProjectInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

internal const val ACC_NO_TEMPLATE_REQUIRED = "The AccNoTemplate property is required for projects"
internal const val ACC_NO_TEMPLATE_INVALID = "The given AccNoTemplate is invalid. Expected pattern is !{TEMPLATE}"

class ProjectInfoService(
    private val context: PersistenceContext,
    private val queryService: SubmissionQueryService,
    private val accNoUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService
) {
    @Suppress("ThrowsCount")
    fun process(request: ProjectRequest): ProjectResponse? {
        if (request.subType != "Project") return null

        val submitter = request.submitter
        val template = request.accNoTemplate
        val accNo = request.accNo

        require(privilegesService.canSubmitProjects(submitter)) { throw UserCanNotSubmitProjectsException(submitter) }
        require(template != null) { throw ProjectInvalidAccNoPatternException(ACC_NO_TEMPLATE_REQUIRED) }
        require(accNoUtil.isPattern(template)) { throw ProjectInvalidAccNoPatternException(ACC_NO_TEMPLATE_INVALID) }
        require(queryService.isNew(accNo).not() || context.accessTagExists(accNo).not()) {
            throw ProjectAlreadyExistingException(accNo)
        }

        val accNoPattern = accNoUtil.getPattern(template)
        require(context.sequenceAccNoPatternExists(accNoPattern).not()) {
            throw ProjectAccNoTemplateAlreadyExistsException(accNoPattern)
        }

        context.createAccNoPatternSequence(accNoPattern)
        context.saveAccessTag(accNo)

        return ProjectResponse(request.accNo)
    }
}

data class ProjectRequest(val submitter: String, val subType: String, val accNoTemplate: String?, val accNo: String)
data class ProjectResponse(val accessTag: String)
