package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ebi.ac.uk.api.dto.UserGroupDto
import ebi.ac.uk.commons.http.builder.linkedMultiValueMapOf
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.postAsync
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.commons.http.ext.put
import ebi.ac.uk.model.Collection
import ebi.ac.uk.model.Group
import org.springframework.web.reactive.function.client.WebClient

private const val GROUP_URL = "/groups"
private const val PROJECTS_URL = "/projects"
private const val FTP_URL = "/submissions/ftp"

class CommonOperationsClient(
    private val client: WebClient,
) : GeneralOperations {
    override fun getGroups(): List<Group> {
        return client.getForObject<Array<Group>>(GROUP_URL).toList()
    }

    override fun getCollections(): List<Collection> {
        return client.getForObject<Array<Collection>>(PROJECTS_URL).toList()
    }

    override suspend fun generateFtpLinks(accNo: String) {
        val body = linkedMultiValueMapOf("accNo" to accNo)
        client.postAsync("$FTP_URL/generate", RequestParams(body = body))
    }

    override fun createGroup(
        groupName: String,
        groupDescription: String,
    ): UserGroupDto {
        val body = linkedMapOf("groupName" to groupName, "description" to groupDescription)
        return client.postForObject(GROUP_URL, RequestParams(body = body))
    }

    override fun addUserInGroup(
        groupName: String,
        userName: String,
    ) {
        val body = linkedMapOf("groupName" to groupName, "userName" to userName)
        client.put(GROUP_URL, RequestParams(body = body))
    }
}
