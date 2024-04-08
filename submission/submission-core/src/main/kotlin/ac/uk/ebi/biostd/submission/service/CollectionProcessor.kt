package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.persistence.common.service.PersistenceService
import ac.uk.ebi.biostd.submission.exceptions.CollectionAccNoTemplateAlreadyExistsException
import ac.uk.ebi.biostd.submission.exceptions.CollectionAlreadyExistingException
import ac.uk.ebi.biostd.submission.exceptions.CollectionInvalidAccNoPatternException
import ac.uk.ebi.biostd.submission.exceptions.UserCanNotSubmitCollectionsException
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ac.uk.ebi.biostd.submission.util.AccNoPatternUtil
import ebi.ac.uk.model.extensions.accNoTemplate
import ebi.ac.uk.security.integration.components.IUserPrivilegesService

internal const val ACC_NO_TEMPLATE_REQUIRED = "The AccNoTemplate property is required for collections"
internal const val ACC_NO_TEMPLATE_INVALID = "The given AccNoTemplate is invalid. Expected pattern is !{TEMPLATE}"

class CollectionProcessor(
    private val service: PersistenceService,
    private val accNoUtil: AccNoPatternUtil,
    private val privilegesService: IUserPrivilegesService,
) {
    fun process(request: SubmitRequest): String {
        val submitter = request.submitter.email
        val accNo = request.submission.accNo
        val template = request.submission.accNoTemplate

        require(privilegesService.canSubmitCollections(submitter)) {
            throw UserCanNotSubmitCollectionsException(submitter)
        }

        validatePattern(template)
        val accNoPattern = accNoUtil.getPattern(template!!)

        if (request.previousVersion == null) {
            validate(accNo, accNoPattern)
            persist(accNo, accNoPattern)
        }

        return accNo
    }

    private fun validate(
        accNo: String,
        accNoPattern: String,
    ) {
        if (service.accessTagExists(accNo)) throw CollectionAlreadyExistingException(accNo)
        if (service.sequenceAccNoPatternExists(accNoPattern)) {
            throw CollectionAccNoTemplateAlreadyExistsException(accNoPattern)
        }
    }

    private fun validatePattern(template: String?) {
        require(template != null) { throw CollectionInvalidAccNoPatternException(ACC_NO_TEMPLATE_REQUIRED) }
        require(accNoUtil.isPattern(template)) { throw CollectionInvalidAccNoPatternException(ACC_NO_TEMPLATE_INVALID) }
    }

    private fun persist(
        accNo: String,
        accNoPattern: String,
    ) {
        service.saveAccessTag(accNo)
        service.createAccNoPatternSequence(accNoPattern)
    }
}
