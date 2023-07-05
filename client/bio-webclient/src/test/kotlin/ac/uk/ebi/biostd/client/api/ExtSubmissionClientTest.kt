package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.dto.ExtPageQuery
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.StorageMode.FIRE
import ebi.ac.uk.model.constants.SUBMISSION
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
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class ExtSubmissionClientTest(
    @MockK private val client: WebClient,
    @MockK private val extSerializationService: ExtSerializationService
) {
    private val testInstance = ExtSubmissionClient(client, extSerializationService)

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `get ext submissions`(
        @MockK extPage: ExtPage
    ) {
        val expectedUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=2"
        val query = ExtPageQuery(limit = 2, offset = 1)

        every { client.getForObject<String>(expectedUrl) } returns "ExtPage"
        every { extSerializationService.deserializePage("ExtPage") } returns extPage

        testInstance.getExtSubmissions(query)
        verify(exactly = 1) {
            client.getForObject<String>(expectedUrl)
            extSerializationService.deserializePage("ExtPage")
        }
    }

    @Test
    fun `ext submissions filtering`(
        @MockK extPage: ExtPage
    ) {
        val from = OffsetDateTime.of(2019, 9, 21, 15, 0, 0, 0, UTC)
        val to = OffsetDateTime.of(2020, 9, 21, 15, 0, 0, 0, UTC)
        val query = ExtPageQuery(limit = 2, offset = 1, fromRTime = from, toRTime = to, released = true)
        val stringFrom = "2019-09-21T15:00:00Z"
        val stringTo = "2020-09-21T15:00:00Z"
        val expectedUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=2&fromRTime=$stringFrom&toRTime=$stringTo&released=true"

        every { client.getForObject<String>(expectedUrl) } returns "ExtPage"
        every { extSerializationService.deserializePage("ExtPage") } returns extPage

        testInstance.getExtSubmissions(query)
        verify(exactly = 1) {
            client.getForObject<String>(expectedUrl)
            extSerializationService.deserializePage("ExtPage")
        }
    }

    @Test
    fun `get ext submissions page`(
        @MockK extPage: ExtPage
    ) {
        val pageUrl = "$EXT_SUBMISSIONS_URL?offset=1&limit=2"

        every { client.getForObject<String>(pageUrl) } returns "ExtPage"
        every { extSerializationService.deserializePage("ExtPage") } returns extPage

        testInstance.getExtSubmissionsPage(pageUrl)
        verify(exactly = 1) {
            client.getForObject<String>(pageUrl)
            extSerializationService.deserializePage("ExtPage")
        }
    }

    @Test
    fun `get ext submission by accNo`(
        @MockK extSubmission: ExtSubmission
    ) {
        val expectedUrl = "$EXT_SUBMISSIONS_URL/S-TEST123?includeFileList=false"

        every { client.getForObject<String>(expectedUrl) } returns "ExtSubmission"
        every { extSerializationService.deserialize("ExtSubmission") } returns extSubmission

        testInstance.getExtByAccNo("S-TEST123")
        verify(exactly = 1) {
            client.getForObject<String>(expectedUrl)
            extSerializationService.deserialize("ExtSubmission")
        }
    }

    @Test
    fun `get referenced files`(
        @MockK extFileTable: ExtFileTable
    ) {
        val filesUrl = "$EXT_SUBMISSIONS_URL/S-TEST123/referencedFiles/file-list"

        every { client.getForObject<String>(filesUrl) } returns "ExtFileTable"
        every { extSerializationService.deserializeTable("ExtFileTable") } returns extFileTable

        testInstance.getReferencedFiles(filesUrl)
        verify(exactly = 1) {
            client.getForObject<String>(filesUrl)
            extSerializationService.deserializeTable("ExtFileTable")
        }
    }

    @Test
    fun `submit ext submission`(
        @MockK extSubmission: ExtSubmission,
        @MockK requestSpec: RequestBodySpec,
    ) {
        val bodySlot = slot<LinkedMultiValueMap<String, Any>>()

        every { extSerializationService.serialize(extSubmission) } returns "ExtSubmission"
        every { extSerializationService.deserialize("ExtSubmission") } returns extSubmission

        every { client.post().uri(EXT_SUBMISSIONS_URL) } returns requestSpec
        every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "ExtSubmission"

        testInstance.submitExt(extSubmission)

        val body = bodySlot.captured
        assertThat(body[SUBMISSION]).hasSize(1)
        assertThat(body[SUBMISSION]!!.first()).isEqualTo("ExtSubmission")
        verify(exactly = 1) {
            requestSpec.bodyValue(body)
            client.post().uri(EXT_SUBMISSIONS_URL)
            extSerializationService.serialize(extSubmission)
            extSerializationService.deserialize("ExtSubmission")
            requestSpec.retrieve().bodyToMono(String::class.java).block()
        }
    }

    @Test
    fun `submit async ext submission`(
        @MockK extSubmission: ExtSubmission,
        @MockK requestSpec: RequestBodySpec,
    ) {
        val bodySlot = slot<LinkedMultiValueMap<String, Any>>()

        every { extSerializationService.serialize(extSubmission) } returns "ExtSubmission"
        every { client.post().uri("$EXT_SUBMISSIONS_URL/async") } returns requestSpec
        every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "ExtSubmission"

        testInstance.submitExtAsync(extSubmission)

        val body = bodySlot.captured
        assertThat(body[SUBMISSION]).hasSize(1)
        assertThat(body[SUBMISSION]!!.first()).isEqualTo("ExtSubmission")
        verify(exactly = 1) {
            requestSpec.bodyValue(body)
            client.post().uri("$EXT_SUBMISSIONS_URL/async")
            extSerializationService.serialize(extSubmission)
            requestSpec.retrieve().bodyToMono(String::class.java).block()
        }
    }

    @Test
    fun `transfer submission`(
        @MockK requestSpec: RequestBodySpec,
    ) {
        every { client.post().uri("$EXT_SUBMISSIONS_URL/S-BSST1/transfer/FIRE") } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "ExtSubmission"

        testInstance.transferSubmission("S-BSST1", FIRE)

        verify(exactly = 1) {
            client.post().uri("$EXT_SUBMISSIONS_URL/S-BSST1/transfer/FIRE")
            requestSpec.retrieve().bodyToMono(String::class.java).block()
        }
    }
}
