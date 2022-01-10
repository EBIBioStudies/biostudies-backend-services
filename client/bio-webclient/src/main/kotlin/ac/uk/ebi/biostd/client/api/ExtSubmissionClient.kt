package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ac.uk.ebi.biostd.client.integration.web.ExtSubmissionOperations
import ebi.ac.uk.commons.http.spring.multiValueMap
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.model.constants.FILE_LISTS
import ebi.ac.uk.model.constants.FILE_MODE
import ebi.ac.uk.model.constants.SUBMISSION
import ebi.ac.uk.util.date.toStringInstant
import ebi.ac.uk.util.web.optionalQueryParam
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
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
    override fun getExtSubmissions(extPageQuery: ExtPageQuery): ExtPage {
        val response = restTemplate.getForEntity<String>(asUrl(extPageQuery)).body!!
        return extSerializationService.deserializePage(response)
    }

    override fun getExtSubmissionsPage(pageUrl: String): ExtPage {
        val response = restTemplate.getForEntity<String>(pageUrl).body!!
        return extSerializationService.deserializePage(response)
    }

    override fun getExtByAccNo(accNo: String): ExtSubmission {
        val response = restTemplate.getForEntity<String>("$EXT_SUBMISSIONS_URL/$accNo").body!!
        return extSerializationService.deserialize(response)
    }

    override fun getReferencedFiles(filesUrl: String): ExtFileTable {
        val response = restTemplate.getForEntity<String>(decode(filesUrl, UTF_8)).body!!
        return extSerializationService.deserializeTable(response)
    }

    override fun submitExt(extSubmission: ExtSubmission, fileLists: List<File>, fileMode: FileMode): ExtSubmission {
        val body = multiValueMap(
            FILE_LISTS to fileLists.map { FileSystemResource(it) },
            SUBMISSION to extSerializationService.serialize(extSubmission),
            FILE_MODE to fileMode.name,
        )
        val response = restTemplate.postForEntity<String>(EXT_SUBMISSIONS_URL, HttpEntity(body))
        return extSerializationService.deserialize(response.body!!)
    }

    override fun submitExtAsync(extSubmission: ExtSubmission, fileLists: List<File>, fileMode: FileMode) {
        val body = multiValueMap(
            FILE_LISTS to fileLists.map { FileSystemResource(it) },
            SUBMISSION to extSerializationService.serialize(extSubmission),
            FILE_MODE to fileMode.name,
        )
        restTemplate.postForEntity<String>("$EXT_SUBMISSIONS_URL/async", HttpEntity(body))
    }

    private fun asUrl(extPageQuery: ExtPageQuery): String =
        UriComponentsBuilder.fromUriString(EXT_SUBMISSIONS_URL)
            .queryParam("offset", extPageQuery.offset)
            .queryParam("limit", extPageQuery.limit)
            .optionalQueryParam("fromRTime", extPageQuery.fromRTime?.toStringInstant())
            .optionalQueryParam("toRTime", extPageQuery.toRTime?.toStringInstant())
            .optionalQueryParam("released", extPageQuery.released)
            .build()
            .toUriString()
}
