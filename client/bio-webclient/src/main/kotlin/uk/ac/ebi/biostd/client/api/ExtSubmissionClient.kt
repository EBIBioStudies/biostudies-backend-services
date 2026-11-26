package uk.ac.ebi.biostd.client.api

import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.util.UriComponentsBuilder
import uk.ac.ebi.biostd.client.dto.ExtPageRequest
import uk.ac.ebi.biostd.client.dto.ExtSubPageQuery
import uk.ac.ebi.biostd.client.integration.web.ExtSubmissionOperations
import uk.ac.ebi.biostd.commons.http.builder.linkedMultiValueMapOf
import uk.ac.ebi.biostd.commons.http.ext.RequestParams
import uk.ac.ebi.biostd.commons.http.ext.getForObject
import uk.ac.ebi.biostd.commons.http.ext.post
import uk.ac.ebi.biostd.commons.http.ext.postForObject
import uk.ac.ebi.biostd.commons.http.ext.postForObjectAsync
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtPage
import uk.ac.ebi.biostd.extended.model.ExtSubmission
import uk.ac.ebi.biostd.extended.model.StorageMode
import uk.ac.ebi.biostd.extended.model.WebExtPage
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.model.SubmissionId
import uk.ac.ebi.biostd.model.SubmissionTransferOptions
import uk.ac.ebi.biostd.model.constants.SUBMISSION
import uk.ac.ebi.biostd.util.date.toStringInstant
import uk.ac.ebi.biostd.util.web.optionalQueryParam

const val EXT_SUBMISSIONS_URL = "/submissions/extended"

@Suppress("TooManyFunctions")
class ExtSubmissionClient(
    private val client: WebClient,
    private val extSerializationService: ExtSerializationService,
) : ExtSubmissionOperations {
    override fun getExtSubmissions(extPageQuery: ExtSubPageQuery): ExtPage {
        val response = client.getForObject<String>(asUrl(extPageQuery))
        return extSerializationService.deserializePage(response)
    }

    override fun getExtSubmissionsPage(pageUrl: String): ExtPage {
        val response = client.getForObject<String>(pageUrl)
        return extSerializationService.deserializePage(response)
    }

    override fun getExtByAccNo(
        accNo: String,
        includeFileList: Boolean,
    ): ExtSubmission {
        val sub = client.getForObject<String>("$EXT_SUBMISSIONS_URL/$accNo?includeFileList=$includeFileList")
        return extSerializationService.deserialize(sub)
    }

    override suspend fun getFileListFiles(filesUrl: String): WebExtPage<ExtFile> =
        client
            .get()
            .uri(filesUrl)
            .retrieve()
            .awaitBody<WebExtPage<ExtFile>>()

    override suspend fun getFileListFiles(
        accNo: String,
        fileList: String,
        page: ExtPageRequest,
    ): WebExtPage<ExtFile> {
        val url =
            UriComponentsBuilder
                .fromUriString("$EXT_SUBMISSIONS_URL/$accNo/fileList/$fileList")
                .optionalQueryParam("offset", page.offset)
                .optionalQueryParam("limit", page.limit)
                .build()
                .toUriString()
        return client
            .get()
            .uri(url)
            .retrieve()
            .awaitBody<WebExtPage<ExtFile>>()
    }

    override suspend fun getLinkListLinks(
        accNo: String,
        linkList: String,
        page: ExtPageRequest,
    ): WebExtPage<ExtLink> {
        val url =
            UriComponentsBuilder
                .fromUriString("$EXT_SUBMISSIONS_URL/$accNo/linkList/$linkList")
                .optionalQueryParam("offset", page.offset)
                .optionalQueryParam("limit", page.limit)
                .build()
                .toUriString()
        return client
            .get()
            .uri(url)
            .retrieve()
            .awaitBody<WebExtPage<ExtLink>>()
    }

    override fun submitExt(extSubmission: ExtSubmission): ExtSubmission {
        val body = linkedMultiValueMapOf(SUBMISSION to extSerializationService.serialize(extSubmission))
        val response = client.postForObject<String>(EXT_SUBMISSIONS_URL, RequestParams(body = body))
        return extSerializationService.deserialize(response)
    }

    override fun submitExtAsync(extSubmission: ExtSubmission) {
        client.post(
            "$EXT_SUBMISSIONS_URL/async",
            RequestParams(body = linkedMultiValueMapOf(SUBMISSION to extSerializationService.serialize(extSubmission))),
        )
    }

    override fun migrateSubmission(
        accNo: String,
        target: StorageMode,
    ) {
        client.post("$EXT_SUBMISSIONS_URL/$accNo/migrate/$target")
    }

    override fun refreshSubmission(accNo: String): SubmissionId = client.postForObject("$EXT_SUBMISSIONS_URL/refresh/$accNo")

    override suspend fun generateDoi(accNo: String): SubmissionId = client.postForObject("$EXT_SUBMISSIONS_URL/$accNo/generate-doi")

    override suspend fun transferSubmissions(options: SubmissionTransferOptions) =
        client.postForObjectAsync<Unit>("$EXT_SUBMISSIONS_URL/transfer", RequestParams(body = options))

    override suspend fun transferEmailUpdate(options: SubmissionTransferOptions) =
        client.postForObjectAsync<Unit>("$EXT_SUBMISSIONS_URL/transfer/email-update", RequestParams(body = options))

    private fun asUrl(extPageQuery: ExtSubPageQuery): String =
        UriComponentsBuilder
            .fromUriString(EXT_SUBMISSIONS_URL)
            .queryParam("offset", extPageQuery.offset)
            .queryParam("limit", extPageQuery.limit)
            .optionalQueryParam("fromRTime", extPageQuery.fromRTime?.toStringInstant())
            .optionalQueryParam("toRTime", extPageQuery.toRTime?.toStringInstant())
            .optionalQueryParam("collection", extPageQuery.collection)
            .optionalQueryParam("released", extPageQuery.released)
            .build()
            .toUriString()
}
