package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPage
import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.constants.FILE_LISTS
import ebi.ac.uk.model.constants.SUBMISSION
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus.OK
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class ExtSubmissionClientTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val restTemplate: RestTemplate,
    @MockK private val extSerializationService: ExtSerializationService
) {
    private val testInstance = ExtSubmissionClient(restTemplate, extSerializationService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get ext submissions`(
        @MockK extPage: ExtPage
    ) {
        val expectedUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=2"
        val response: ResponseEntity<String> = ResponseEntity("ExtPage", OK)
        val query = ExtPageQuery(limit = 2, offset = 1)

        every { restTemplate.getForEntity(expectedUrl, String::class.java) } returns response
        every { extSerializationService.deserialize("ExtPage", ExtPage::class.java) } returns extPage

        testInstance.getExtSubmissions(query)
        verify(exactly = 1) { restTemplate.getForEntity(expectedUrl, String::class.java) }
        verify(exactly = 1) { extSerializationService.deserialize("ExtPage", ExtPage::class.java) }
    }

    @Test
    fun `ext submissions filtering`(
        @MockK extPage: ExtPage
    ) {
        val from = OffsetDateTime.of(2019, 9, 21, 15, 0, 0, 0, UTC)
        val to = OffsetDateTime.of(2020, 9, 21, 15, 0, 0, 0, UTC)
        val response: ResponseEntity<String> = ResponseEntity("ExtPage", OK)
        val query = ExtPageQuery(limit = 2, offset = 1, fromRTime = from, toRTime = to, released = true)
        val stringFrom = "2019-09-21T15:00:00Z"
        val stringTo = "2020-09-21T15:00:00Z"
        val expectedUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=2&fromRTime=$stringFrom&toRTime=$stringTo&released=true"

        every { restTemplate.getForEntity(expectedUrl, String::class.java) } returns response
        every { extSerializationService.deserialize("ExtPage", ExtPage::class.java) } returns extPage

        testInstance.getExtSubmissions(query)
        verify(exactly = 1) { restTemplate.getForEntity(expectedUrl, String::class.java) }
        verify(exactly = 1) { extSerializationService.deserialize("ExtPage", ExtPage::class.java) }
    }

    @Test
    fun `get ext submissions page`(
        @MockK extPage: ExtPage
    ) {
        val pageUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=2"
        val response: ResponseEntity<String> = ResponseEntity("ExtPage", OK)

        every { restTemplate.getForEntity(pageUrl, String::class.java) } returns response
        every { extSerializationService.deserialize("ExtPage", ExtPage::class.java) } returns extPage

        testInstance.getExtSubmissionsPage(pageUrl)
        verify(exactly = 1) { restTemplate.getForEntity(pageUrl, String::class.java) }
        verify(exactly = 1) { extSerializationService.deserialize("ExtPage", ExtPage::class.java) }
    }

    @Test
    fun `get ext submission by accNo`(
        @MockK extSubmission: ExtSubmission
    ) {
        val response: ResponseEntity<String> = ResponseEntity("ExtSubmission", OK)

        every { restTemplate.getForEntity("$EXT_SUBMISSIONS_URL/S-TEST123", String::class.java) } returns response
        every { extSerializationService.deserialize("ExtSubmission", ExtSubmission::class.java) } returns extSubmission

        testInstance.getExtByAccNo("S-TEST123")
        verify(exactly = 1) { restTemplate.getForEntity("$EXT_SUBMISSIONS_URL/S-TEST123", String::class.java) }
        verify(exactly = 1) { extSerializationService.deserialize("ExtSubmission", ExtSubmission::class.java) }
    }

    @Test
    fun `get referenced files`(
        @MockK extFileTable: ExtFileTable
    ) {
        val filesUrl = "$EXT_SUBMISSIONS_URL/S-TEST123/fileList/file-list/files"
        val response: ResponseEntity<String> = ResponseEntity("ExtFileTable", OK)

        every { restTemplate.getForEntity(filesUrl, String::class.java) } returns response
        every { extSerializationService.deserialize("ExtFileTable", ExtFileTable::class.java) } returns extFileTable

        testInstance.getReferencedFiles(filesUrl)
        verify(exactly = 1) { restTemplate.getForEntity(filesUrl, String::class.java) }
        verify(exactly = 1) { extSerializationService.deserialize("ExtFileTable", ExtFileTable::class.java) }
    }

    @Test
    fun `submit ext submission`(
        @MockK extSubmission: ExtSubmission
    ) {
        val entity = slot<HttpEntity<String>>()
        val fileList = tempFolder.createFile("file-list.tsv")
        val response: ResponseEntity<String> = ResponseEntity("ExtSubmission", OK)

        every { extSerializationService.serialize(extSubmission) } returns "ExtSubmission"
        every { extSerializationService.deserialize("ExtSubmission", ExtSubmission::class.java) } returns extSubmission
        every { restTemplate.postForEntity(EXT_SUBMISSIONS_URL, capture(entity), String::class.java) } returns response

        testInstance.submitExt(extSubmission, listOf(fileList))

        val captured = entity.captured
        assertHttpEntity(captured, fileList)
        verify(exactly = 1) { extSerializationService.serialize(extSubmission) }
        verify(exactly = 1) { extSerializationService.deserialize("ExtSubmission", ExtSubmission::class.java) }
        verify(exactly = 1) { restTemplate.postForEntity(EXT_SUBMISSIONS_URL, captured, String::class.java) }
    }

    @Test
    fun `submit async ext submission`(
        @MockK extSubmission: ExtSubmission
    ) {
        val entity = slot<HttpEntity<String>>()
        val fileList = tempFolder.createFile("async-file-list.tsv")
        val response: ResponseEntity<String> = ResponseEntity("ExtSubmission", OK)

        every { extSerializationService.serialize(extSubmission) } returns "ExtSubmission"
        every {
            restTemplate.postForEntity("$EXT_SUBMISSIONS_URL/async", capture(entity), String::class.java)
        } returns response

        testInstance.submitExtAsync(extSubmission, listOf(fileList))

        val captured = entity.captured
        assertHttpEntity(captured, fileList)
        verify(exactly = 1) { extSerializationService.serialize(extSubmission) }
        verify(exactly = 1) { restTemplate.postForEntity("$EXT_SUBMISSIONS_URL/async", captured, String::class.java) }
    }

    private fun assertHttpEntity(entity: HttpEntity<String>, fileList: File) {
        val body = entity.body as LinkedMultiValueMap<String, Any>
        assertThat(body).hasSize(2)
        assertThat(body[FILE_LISTS]).hasSize(1)
        assertThat((body[FILE_LISTS]?.first() as FileSystemResource).file).isEqualTo(fileList)
        assertThat(body[SUBMISSION]).hasSize(1)
        assertThat(body[SUBMISSION]?.first()).isEqualTo("ExtSubmission")
    }
}
