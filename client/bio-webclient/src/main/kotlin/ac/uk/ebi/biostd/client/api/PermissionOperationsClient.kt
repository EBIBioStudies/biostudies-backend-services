package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.PermissionOperations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity

class PermissionOperationsClient(
    private val client: WebClient,
) : PermissionOperations {
    override suspend fun grantPermission(
        user: String,
        accNo: String,
        accessType: String,
    ) {
        client
            .put()
            .uri(PERMISSIONS_URL)
            .bodyValue(hashMapOf("userEmail" to user, "accessType" to accessType, "accNo" to accNo))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun revokePermission(
        user: String,
        accNo: String,
        accessType: String,
    ) {
        client
            .post()
            .uri("$PERMISSIONS_URL/revoke")
            .bodyValue(hashMapOf("userEmail" to user, "accessType" to accessType, "accNo" to accNo))
            .retrieve()
            .awaitBodilessEntity()
    }

    companion object {
        private const val PERMISSIONS_URL = "/permissions"
    }
}
