package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPage
import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.date.toStringInstant
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

const val EXT_SUBMISSIONS_URL = "/submissions/extended"

class ExtSubmissionClient(
    private val restTemplate: RestTemplate,
    private val extSerializationService: ExtSerializationService
) : ExtSubmissionOperations {
    override fun getExtSubmissions(extPageQuery: ExtPageQuery): ExtPage =
        restTemplate
            .getForEntity<String>(asUrl(extPageQuery))
            .deserialized()

    override fun getExtSubmissionsPage(pageUrl: String): ExtPage =
        restTemplate
            .getForEntity<String>(pageUrl)
            .deserialized()

    override fun getExtByAccNo(accNo: String): ExtSubmission =
        restTemplate
            .getForEntity<String>("$EXT_SUBMISSIONS_URL/$accNo")
            .deserialized()

    override fun submitExt(extSubmission: ExtSubmission): ExtSubmission =
        restTemplate
            .postForEntity<String>(EXT_SUBMISSIONS_URL, HttpEntity(extSerializationService.serialize(extSubmission)))
            .deserialized()

    override fun submitExtDirect(extSubmission: String): ExtSubmission =
        restTemplate
            .postForEntity<String>(EXT_SUBMISSIONS_URL, HttpEntity(extSubmission))
            .deserialized()

    private fun asUrl(extPageQuery: ExtPageQuery): String =
        UriComponentsBuilder.fromUriString(EXT_SUBMISSIONS_URL)
            .queryParam("offset", extPageQuery.offset)
            .queryParam("limit", extPageQuery.limit)
            .optionalQueryParam("fromRTime", extPageQuery.fromRTime?.toStringInstant())
            .optionalQueryParam("toRTime", extPageQuery.toRTime?.toStringInstant())
            .optionalQueryParam("released", extPageQuery.released)
            .build()
            .toUriString()

    private inline fun <reified T> ResponseEntity<String>.deserialized(): T =
        map { body -> extSerializationService.deserialize(body, T::class.java) }.body!!
}
