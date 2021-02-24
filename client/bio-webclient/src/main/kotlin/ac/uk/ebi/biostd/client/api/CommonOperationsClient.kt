package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GeneralOperations
import ebi.ac.uk.model.Collection
import ebi.ac.uk.model.Group
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

private const val GROUP_URL = "/groups"
private const val PROJECTS_URL = "/projects"

class CommonOperationsClient(private val template: RestTemplate) : GeneralOperations {
    override fun getGroups(): List<Group> = template.getForObject<Array<Group>>(GROUP_URL).toList()

    override fun getProjects(): List<Collection> = template.getForObject<Array<Collection>>(PROJECTS_URL).toList()
}
