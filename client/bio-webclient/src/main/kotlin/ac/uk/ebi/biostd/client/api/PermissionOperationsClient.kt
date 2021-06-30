package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.PermissionOperations
import org.springframework.web.client.RestTemplate

class PermissionOperationsClient(private val restTemplate: RestTemplate) :
    PermissionOperations {
    override fun givePermissionToUser(userEmail: String, accessTagName: String, accessType: String) {
        val body = hashMapOf("userEmail" to userEmail, "accessType" to accessType, "accessTagName" to accessTagName)
        restTemplate.put(PERMISSIONS_URL, body)
    }

    companion object {
        private const val PERMISSIONS_URL = "/permissions"
    }
}
