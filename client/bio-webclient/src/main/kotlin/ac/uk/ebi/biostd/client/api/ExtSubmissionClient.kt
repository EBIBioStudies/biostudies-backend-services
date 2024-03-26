package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ebi.ac.uk.commons.http.builder.linkedMultiValueMapOf
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.util.date.toStringInstant
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils.decode
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.nio.charset.StandardCharsets.UTF_8
import java.time.Instant

const val EXT_SUBMISSIONS_URL = "/submissions/extended"

class ExtSubmissionClient(
    private val client: WebClient,
    private val extSerializationService: ExtSerializationService,
) : ExtSubmissionOperations {
    override fun getExtSubmissions(extPageQuery: ExtPageQuery): ExtPage {
        val response = client.getForObject<String>(asUrl(extPageQuery))
        return extSerializationService.deserializePage(response)
    }

    override fun getExtSubmissionsPage(pageUrl: String): ExtPage {
        val response = client.getForObject<String>(pageUrl)
        return extSerializationService.deserializePage(response)
    }

    override fun getExtByAccNo(accNo: String, includeFileList: Boolean): ExtSubmission {
        val sub = client.getForObject<String>("$EXT_SUBMISSIONS_URL/$accNo?includeFileList=$includeFileList")
        return extSerializationService.deserialize(sub)
    }

    override fun getReferencedFiles(filesUrl: String): ExtFileTable {
        val response = client.getForObject<String>(decode(filesUrl, UTF_8))
        return extSerializationService.deserializeTable(response)
    }

    override fun submitExt(extSubmission: ExtSubmission): ExtSubmission {
        val body = linkedMultiValueMapOf(SUBMISSION to extSerializationService.serialize(extSubmission))
        val response = client.postForObject<String>(EXT_SUBMISSIONS_URL, RequestParams(body = body))
        return extSerializationService.deserialize(response)
    }

    override fun submitExtAsync(extSubmission: ExtSubmission) {
        client.post(
            "$EXT_SUBMISSIONS_URL/async",
            RequestParams(body = linkedMultiValueMapOf(SUBMISSION to extSerializationService.serialize(extSubmission)))
        )
    }

    override fun transferSubmission(accNo: String, target: StorageMode) {
        client.post("$EXT_SUBMISSIONS_URL/$accNo/transfer/$target")
    }

    override fun refreshSubmission(accNo: String): Pair<String, Int> {
        return client.postForObject("$EXT_SUBMISSIONS_URL/refresh/$accNo")
    }

    override fun releaseSubmission(accNo: String, releaseDate: Instant): Pair<String, Int> {
        return client.postForObject("$EXT_SUBMISSIONS_URL/release/$accNo/$releaseDate")
    }

    private fun asUrl(extPageQuery: ExtPageQuery): String =
        UriComponentsBuilder.fromUriString(EXT_SUBMISSIONS_URL)
            .queryParam("offset", extPageQuery.offset)
            .queryParam("limit", extPageQuery.limit)
            .optionalQueryParam("fromRTime", extPageQuery.fromRTime?.toStringInstant())
            .optionalQueryParam("toRTime", extPageQuery.toRTime?.toStringInstant())
            .optionalQueryParam("collection", extPageQuery.collection)
            .optionalQueryParam("released", extPageQuery.released)
            .build()
            .toUriString()
}
