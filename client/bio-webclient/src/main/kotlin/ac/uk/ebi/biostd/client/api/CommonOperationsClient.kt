package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ebi.ac.uk.api.dto.UserGroupDto
import ebi.ac.uk.model.Collection
import ebi.ac.uk.model.Group
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity
import org.springframework.web.client.postForObject

private const val GROUP_URL = "/groups"
private const val PROJECTS_URL = "/projects"
private const val FTP_URL = "/submissions/ftp"

class CommonOperationsClient(private val template: RestTemplate) : GeneralOperations {
    override fun getGroups(): List<Group> = template.getForObject<Array<Group>>(GROUP_URL).toList()

    override fun getCollections(): List<Collection> = template.getForObject<Array<Collection>>(PROJECTS_URL).toList()

    override fun generateFtpLink(relPath: String) {
        val body = LinkedMultiValueMap<String, String>(mapOf("relPath" to listOf(relPath)))
        template.postForEntity<String>("$FTP_URL/generate", body)
    }

    override fun createGroup(groupName: String, description: String): UserGroupDto {
        val body = linkedMapOf("groupName" to groupName, "description" to description)
        return template.postForObject(GROUP_URL, body)
    }

    override fun addUserInGroup(groupName: String, userName: String) {
        val body = linkedMapOf("userName" to userName)
        template.put("$GROUP_URL/$groupName", body)
    }
}
