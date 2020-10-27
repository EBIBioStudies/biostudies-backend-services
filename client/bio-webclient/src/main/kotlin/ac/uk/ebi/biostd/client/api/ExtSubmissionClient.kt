package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPage
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.util.date.toStringInstant
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.OffsetDateTime

const val EXT_SUBMISSIONS_URL = "/submissions/extended"

class ExtSubmissionClient(
    private val restTemplate: RestTemplate,
    private val extSerializationService: ExtSerializationService
) : ExtSubmissionOperations {
    override fun getExtSubmissions(
        limit: Int,
        offset: Int,
        fromRTime: OffsetDateTime?,
        toRTime: OffsetDateTime?
    ): ExtPage =
        restTemplate
            .getForEntity<String>(asUrl(limit, offset, fromRTime, toRTime))
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

    private fun asUrl(limit: Int, offset: Int, fromRTime: OffsetDateTime?, toRTime: OffsetDateTime?): String =
        UriComponentsBuilder.fromUriString(EXT_SUBMISSIONS_URL).apply {
            queryParam("offset", offset)
            queryParam("limit", limit)
            fromRTime?.let { queryParam("fromRTime=${it.toStringInstant()}") }
            toRTime?.let { queryParam("toRTime=${it.toStringInstant()}") }
        }.build().toString()

    private inline fun <reified T> ResponseEntity<String>.deserialized(): T =
        map { body -> extSerializationService.deserialize<T>(body) }.body!!
}
