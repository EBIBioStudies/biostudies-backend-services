package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.PermissionOperations
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.put
import org.springframework.web.reactive.function.client.WebClient

class PermissionOperationsClient(
    private val client: WebClient,
) : PermissionOperations {
    override fun grantPermission(
        user: String,
        accNo: String,
        accessType: String,
    ) {
        val body = hashMapOf("userEmail" to user, "accessType" to accessType, "accNo" to accNo)
        client.put(PERMISSIONS_URL, RequestParams(body = body))
    }

    companion object {
        private const val PERMISSIONS_URL = "/permissions"
    }
}
