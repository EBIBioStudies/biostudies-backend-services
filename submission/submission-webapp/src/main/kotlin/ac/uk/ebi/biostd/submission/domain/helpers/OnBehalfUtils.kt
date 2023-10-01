package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.submission.web.model.OnBehalfRequest
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.security.integration.components.ISecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class OnBehalfUtils(
    private val securityQueryService: ISecurityQueryService,
) {
    fun getOnBehalfUser(request: OnBehalfRequest): SecurityUser {
        val register = request.register.orFalse()
        return if (register) registerInactive(request) else securityQueryService.getUser(request.userEmail)
    }

    private fun registerInactive(registerRequest: OnBehalfRequest): SecurityUser {
        requireNotNull(registerRequest.userName) { "A valid user name must be provided for registration" }
        return securityQueryService.getOrCreateInactive(registerRequest.userEmail, registerRequest.userName)
    }
}
