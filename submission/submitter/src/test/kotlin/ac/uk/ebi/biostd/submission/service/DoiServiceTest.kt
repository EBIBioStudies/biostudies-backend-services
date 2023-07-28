package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.DoiProperties
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorNameException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgNamesException
import ac.uk.ebi.biostd.submission.exceptions.MissingAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.MissingDoiFieldException
import ac.uk.ebi.biostd.submission.exceptions.MissingTitleException
import arrow.core.Either.Companion.left
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class DoiServiceTest(
    @MockK private val webClient: WebClient,
) {
    private val testInstance = DoiService(webClient, properties)
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 10, 11, 0, 0, UTC).toInstant()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns mockNow
    }

    @Test
    fun `doi registration`(
        @MockK requestSpec: RequestBodySpec,
    ) {
        val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
        val org = section("Organization", "o1", "Name" to "EMBL")
        val author = section("Author", null, "Name" to "John Doe", "Affiliation" to "o1", "ORCID" to "12-32-45-82")
        val rootSection = ExtSection(type = "Study", sections = listOf(left(org), left(author)))
        val submission = basicExtSubmission.copy(section = rootSection)

        every { webClient.post().uri(properties.endpoint) } returns requestSpec
        every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "OK"

        testInstance.registerDoi(submission)

        val body = bodySlot.captured
        val requestFile = body[FILE_PARAM]!!.first() as FileSystemResource
        val expectedXml = Files.readString(Paths.get("src/test/resources/ExpectedDOIRequest.xml"))
        assertThat(requestFile.file.readText()).isEqualToIgnoringWhitespace(expectedXml)
        assertThat(body[USER_PARAM]!!.first()).isEqualTo(properties.user)
        assertThat(body[PASSWORD_PARAM]!!.first()).isEqualTo(properties.password)
        assertThat(body[OPERATION_PARAM]!!.first()).isEqualTo(OPERATION_PARAM_VALUE)
        verify(exactly = 1) {
            webClient.post().uri(properties.endpoint)
            requestSpec.bodyValue(body)
            requestSpec.retrieve().bodyToMono(String::class.java).block()
        }
    }

    @Test
    fun `missing title`() {
        val submission = basicExtSubmission.copy(title = null)
        val exception = assertThrows<MissingTitleException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("A title is required for DOI registration")
    }

    @Test
    fun `missing organization`() {
        val submission = basicExtSubmission
        val exception = assertThrows<MissingDoiFieldException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("The required DOI field 'organization' could not be found")
    }

    @Test
    fun `missing organization accNo`() {
        val org = section("Organization", null, "Name" to "EMBL")
        val rootSection = ExtSection(type = "Study", sections = listOf(left(org)))
        val submission = basicExtSubmission.copy(section = rootSection)
        val exception = assertThrows<InvalidOrgException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("Organizations are required to have an accession")
    }

    @Test
    fun `missing organization name`() {
        val org1 = section("Organization", "o1", "Institute" to "EMBL")
        val org2 = section("Organization", "o2", "Name" to "EMBL-EBI")
        val org3 = section("Organization", "o3", "Institute" to "American Society")
        val rootSection = ExtSection(type = "Study", sections = listOf(left(org1), left(org2), left(org3)))
        val submission = basicExtSubmission.copy(section = rootSection)
        val exception = assertThrows<InvalidOrgNamesException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("The following organization names are empty: o1, o3")
    }

    @Test
    fun `missing author name`() {
        val org = section("Organization", "o1", "Name" to "EMBL")
        val author = section("Author", null, "P.I." to "John Doe", "Affiliation" to "o1", "ORCID" to "12-32-45-82")
        val rootSection = ExtSection(type = "Study", sections = listOf(left(org), left(author)))
        val submission = basicExtSubmission.copy(section = rootSection)
        val exception = assertThrows<InvalidAuthorNameException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("Authors are required to have a name")
    }

    @Test
    fun `missing affiliation`() {
        val org = section("Organization", "o1", "Name" to "EMBL")
        val author = section("Author", null, "Name" to "John Doe", "ORCID" to "12-32-45-82")
        val rootSection = ExtSection(type = "Study", sections = listOf(left(org), left(author)))
        val submission = basicExtSubmission.copy(section = rootSection)
        val exception = assertThrows<MissingAuthorAffiliationException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("Authors are required to have an affiliation")
    }

    @Test
    fun `invalid affiliation`() {
        val org = section("Organization", "o1", "Name" to "EMBL")
        val author = section("Author", null, "Name" to "John Doe", "Affiliation" to "o2", "ORCID" to "12-32-45-82")
        val rootSection = ExtSection(type = "Study", sections = listOf(left(org), left(author)))
        val submission = basicExtSubmission.copy(section = rootSection)
        val exception = assertThrows<InvalidAuthorAffiliationException> { testInstance.registerDoi(submission) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message)
            .isEqualTo("The organization 'o2' affiliated to the author 'John Doe' could not be found")
    }

    private fun section(type: String, accNo: String?, vararg attributes: Pair<String, String>) =
        ExtSection(type = type, accNo = accNo, attributes = attributes.map { ExtAttribute(it.first, it.second) })

    companion object {
        private val properties = DoiProperties(
            endpoint = "https://test-endpoint.org",
            uiUrl = "https://www.biostudies.ac.uk",
            user = "a-user",
            password = "a-password",
        )
    }
}
