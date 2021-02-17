package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ebi.ac.uk.model.Group
import ebi.ac.uk.model.Project
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity

private const val GROUP_URL = "/groups"
private const val PROJECTS_URL = "/projects"
private const val FTP_URL = "/submissions/ftp"

class CommonOperationsClient(private val template: RestTemplate) : GeneralOperations {
    override fun getGroups(): List<Group> = template.getForObject<Array<Group>>(GROUP_URL).toList()

    override fun getProjects(): List<Project> = template.getForObject<Array<Project>>(PROJECTS_URL).toList()

    override fun generateFtpLink(relPath: String) {
        val body = LinkedMultiValueMap<String, String>(mapOf("relPath" to listOf(relPath)))
        template.postForEntity<String>("$FTP_URL/generate", body)
    }
}
