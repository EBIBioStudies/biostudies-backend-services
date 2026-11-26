package ac.uk.ebi.biostd.handlers.api

import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.biostd.commons.http.ext.getForObject
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.ExtUser
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService

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
