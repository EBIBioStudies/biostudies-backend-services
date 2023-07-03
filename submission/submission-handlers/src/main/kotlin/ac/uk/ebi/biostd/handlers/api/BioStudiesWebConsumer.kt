package ac.uk.ebi.biostd.handlers.api

import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtUser
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

class BioStudiesWebConsumer(
    private val client: WebClient,
    private val extSerializationService: ExtSerializationService,
) {
    fun getExtSubmission(url: String): ExtSubmission {
        return extSerializationService.deserialize(client.getForObject<String>(url))
    }

    fun getExtUser(url: String): ExtUser {
        return client.getForObject(url)
    }
}
