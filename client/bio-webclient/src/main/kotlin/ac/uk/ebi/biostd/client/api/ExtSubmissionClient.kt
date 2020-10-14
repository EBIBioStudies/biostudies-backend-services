package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPage
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.http.HttpEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForEntity
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

const val EXT_SUBMISSIONS_URL = "/submissions/extended"

class ExtSubmissionClient(
    private val restTemplate: RestTemplate,
    private val extSerializationService: ExtSerializationService
) : ExtSubmissionOperations {
    override fun getSubmissions(
        limit: Int,
        offset: Int,
        fromRTime: String?,
        toRTime: String?
    ): ExtPage = restTemplate.getForObject(asUrl(limit, offset, fromRTime, toRTime))

    override fun getSubmissionsPage(pageUrl: String): ExtPage = restTemplate.getForObject(pageUrl)

    override fun getByAccNo(accNo: String): ExtSubmission = restTemplate.getForObject("$EXT_SUBMISSIONS_URL/$accNo")

    override fun submit(extSubmission: ExtSubmission): ExtSubmission =
        restTemplate.postForEntity<String>(
            EXT_SUBMISSIONS_URL, HttpEntity(extSerializationService.serialize(extSubmission)))
            .map { body -> extSerializationService.deserialize<ExtSubmission>(body) }
            .body!!

    private fun asUrl(limit: Int, offset: Int, fromReleaseDate: String?, toReleaseDate: String?): String {
        val url = StringBuilder("$EXT_SUBMISSIONS_URL?offset=$offset&limit=$limit")
        fromReleaseDate?.let { url.append("&fromRTime=$it") }
        toReleaseDate?.let { url.append("&fromRTime=$it") }

        return url.toString()
    }
}
