package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.api.security.GetOrRegisterUserRequest
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class OnBehalfUtils(
    private val securityQueryService: ISecurityQueryService,
) {
    fun getOnBehalfUser(onBehalfRequest: OnBehalfRequest): SecurityUser {
        val request = onBehalfRequest.asRegisterRequest()
        return if (request.register) registerInactive(request) else securityQueryService.getUser(request.userEmail)
    }

    private fun registerInactive(registerRequest: GetOrRegisterUserRequest): SecurityUser {
        requireNotNull(registerRequest.userName) { "A valid user name must be provided for registration" }
        return securityQueryService.getOrCreateInactive(registerRequest.userEmail, registerRequest.userName!!)
    }
}
