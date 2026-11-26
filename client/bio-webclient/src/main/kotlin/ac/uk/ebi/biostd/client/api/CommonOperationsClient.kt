package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import uk.ac.ebi.biostd.api.dto.UserGroupDto
import uk.ac.ebi.biostd.api.security.UserProfile
import uk.ac.ebi.biostd.model.Collection
import uk.ac.ebi.biostd.model.Group

private const val GROUP_URL = "/groups"
private const val PROJECTS_URL = "/projects"

class CommonOperationsClient(
    private val client: WebClient,
) : GeneralOperations {
    override suspend fun getGroups(): List<Group> =
        client
            .get()
            .uri(GROUP_URL)
            .retrieve()
            .awaitBody()

    override suspend fun getCollections(): List<Collection> =
        client
            .get()
            .uri(PROJECTS_URL)
            .retrieve()
            .awaitBody()

    override suspend fun createGroup(
        groupName: String,
        groupDescription: String,
    ): UserGroupDto =
        client
            .post()
            .uri(GROUP_URL)
            .bodyValue(linkedMapOf("groupName" to groupName, "description" to groupDescription))
            .retrieve()
            .awaitBody()

    override suspend fun addUserInGroup(
        groupName: String,
        userName: String,
    ) {
        client
            .put()
            .uri(GROUP_URL)
            .bodyValue(linkedMapOf("groupName" to groupName, "userName" to userName))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun getProfile(): UserProfile =
        client
            .get()
            .uri("/auth/profile")
            .retrieve()
            .awaitBody()
}
