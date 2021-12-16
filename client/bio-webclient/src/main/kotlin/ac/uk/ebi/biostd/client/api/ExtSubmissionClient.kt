package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPage
import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.extensions.map
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.constants.FILE_LISTS
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.util.date.toStringInstant
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForEntity
import org.springframework.web.client.postForEntity
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils.decode
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

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

    override fun getReferencedFiles(filesUrl: String): ExtFileTable {
        return restTemplate
            .getForEntity<String>(decode(filesUrl, UTF_8))
            .deserialized()
    }

    override fun submitExt(extSubmission: ExtSubmission, fileLists: List<File>, fileMode: FileMode): ExtSubmission =
        restTemplate
            .postForEntity<String>(
                EXT_SUBMISSIONS_URL,
                HttpEntity(getMultipartBody(extSubmission, fileLists, fileMode))
            )
            .deserialized()

    override fun submitExtAsync(extSubmission: ExtSubmission, fileLists: List<File>, fileMode: FileMode) {
        restTemplate.postForEntity<String>(
            "$EXT_SUBMISSIONS_URL/async",
            HttpEntity(getMultipartBody(extSubmission, fileLists, fileMode))
        )
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

    private fun getMultipartBody(extSubmission: ExtSubmission, files: List<File>, fileMode: FileMode) =
        LinkedMultiValueMap(
            files.map { FILE_LISTS to FileSystemResource(it) }
                .plus(SUBMISSION to extSerializationService.serialize(extSubmission))
                .plus(FILE_MODE to fileMode.name)
                .groupBy({ it.first }, { it.second })
        )

    private inline fun <reified T> ResponseEntity<String>.deserialized(): T =
        map { body -> extSerializationService.deserialize(body, T::class.java) }.body!!
}
