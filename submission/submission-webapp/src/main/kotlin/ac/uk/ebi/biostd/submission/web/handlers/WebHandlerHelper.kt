package ac.uk.ebi.biostd.submission.web.handlers

import ac.uk.ebi.biostd.submission.exceptions.ConcurrentProcessingSubmissionException
import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.extended.model.ExtProcessingStatus.PROCESSED
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class WebHandlerHelper(
    private val securityQueryService: ISecurityQueryService
) {
    fun requireProcessed(sub: ExtSubmission) =
        require(sub.status == PROCESSED) { throw ConcurrentProcessingSubmissionException(sub.accNo) }

    fun getOnBehalfUser(onBehalfRequest: OnBehalfRequest): SecurityUser {
        val request = onBehalfRequest.asRegisterRequest()
        return if (request.register) registerInactive(request) else securityQueryService.getUser(request.userEmail)
    }

    private fun registerInactive(registerRequest: GetOrRegisterUserRequest): SecurityUser {
        requireNotNull(registerRequest.userName) { "A valid user name must be provided for registration" }
        return securityQueryService.getOrCreateInactive(registerRequest.userEmail, registerRequest.userName!!)
    }
}
