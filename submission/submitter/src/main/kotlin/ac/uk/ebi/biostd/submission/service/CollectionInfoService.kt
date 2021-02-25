package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.CollectionAccNoTemplateAlreadyExistsException
import ac.uk.ebi.biostd.submission.exceptions.CollectionAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitProjectsException
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

internal const val ACC_NO_TEMPLATE_REQUIRED = "The AccNoTemplate property is required for projects"
internal const val ACC_NO_TEMPLATE_INVALID = "The given AccNoTemplate is invalid. Expected pattern is !{TEMPLATE}"

class CollectionInfoService(
    private val service: PersistenceService,
    private val accNoUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService
) {
    fun process(request: CollectionRequest): CollectionResponse? {
        val (submitter, subType, template, accNo, isNew) = request

        if (subType != "Project") return null

        require(privilegesService.canSubmitProjects(submitter)) { throw UserCanNotSubmitProjectsException(submitter) }
        validatePattern(template)

        val accNoPattern = accNoUtil.getPattern(template!!)
        validateProject(isNew, accNo, accNoPattern)
        persist(isNew, accNo, accNoPattern)

        return CollectionResponse(request.accNo)
    }

    private fun validateProject(isNew: Boolean, accNo: String, accNoPattern: String) {
        if (isNew && service.accessTagExists(accNo)) throw CollectionAlreadyExistingException(accNo)
        if (isNew && service.sequenceAccNoPatternExists(accNoPattern))
            throw CollectionAccNoTemplateAlreadyExistsException(accNoPattern)
    }

    private fun validatePattern(template: String?) {
        require(template != null) { throw CollectionInvalidAccNoPatternException(ACC_NO_TEMPLATE_REQUIRED) }
        require(accNoUtil.isPattern(template)) { throw CollectionInvalidAccNoPatternException(ACC_NO_TEMPLATE_INVALID) }
    }

    private fun persist(isNew: Boolean, accNo: String, accNoPattern: String) {
        if (isNew) {
            service.saveAccessTag(accNo)
            service.createAccNoPatternSequence(accNoPattern)
        }
    }
}

data class CollectionRequest(
    val submitter: String,
    val subType: String,
    val accNoTemplate: String?,
    val accNo: String,
    val isNew: Boolean = true
)

data class CollectionResponse(val accessTag: String)
