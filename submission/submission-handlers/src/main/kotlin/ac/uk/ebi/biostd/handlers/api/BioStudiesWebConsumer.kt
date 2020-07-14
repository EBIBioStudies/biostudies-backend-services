package ac.uk.ebi.biostd.handlers.api

import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class BioStudiesWebConsumer(
    private val restTemplate: RestTemplate,
    private val extSerializationService: ExtSerializationService
) {
    fun getExtSubmission(url: String): ExtSubmission =
        extSerializationService.deserialize(restTemplate.getForObject(url))
}
