package ac.uk.ebi.biostd.submission.domain.helpers

import ebi.ac.uk.api.OnBehalfParameters
import ebi.ac.uk.base.orFalse
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class OnBehalfUtils(
    private val securityQueryService: SecurityQueryService,
) {
    fun getOnBehalfUser(params: OnBehalfParameters): SecurityUser {
        val register = params.register.orFalse()
        return if (register) registerInactive(params) else securityQueryService.getUser(params.userEmail)
    }

    private fun registerInactive(params: OnBehalfParameters): SecurityUser {
        val userName = requireNotNull(params.userName) { "A valid user name must be provided for registration" }
        return securityQueryService.getOrCreateInactive(params.userEmail, userName)
    }
}
