package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.DoiProperties
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorNameException
import ac.uk.ebi.biostd.submission.exceptions.InvalidDoiException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgNamesException
import ac.uk.ebi.biostd.submission.exceptions.MissingAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.MissingDoiFieldException
import ac.uk.ebi.biostd.submission.exceptions.MissingTitleException
import ac.uk.ebi.biostd.submission.model.DoiRequest.Companion.BS_DOI_ID
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.extensions.title
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
    @MockK private val submitRequest: SubmitRequest,
    @MockK private val previousVersion: ExtSubmission,
) {
    private val testInstance = DoiService(webClient, properties)
    private val mockNow = OffsetDateTime.of(2020, 9, 21, 10, 11, 0, 0, UTC).toInstant()

    @AfterEach
    fun afterEach() = clearAllMocks()

    @BeforeEach
    fun beforeEach() {
        mockkStatic(Instant::class)
        every { Instant.now() } returns mockNow
        every { submitRequest.previousVersion } returns null
    }

    @Test
    fun `doi registration`(
        @MockK requestSpec: RequestBodySpec,
    ) {
        val bodySlot = slot<LinkedMultiValueMap<String, Any>>()
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Organization") {
                    accNo = "o1"
                    attribute("Name", "EMBL")
                }

                section("Author") {
                    attribute("Name", "John Doe")
                    attribute("ORCID", "12-32-45-82")
                    attribute("Affiliation", "o1", ref = true)
                }
            }
        }

        every { submitRequest.submission } returns submission
        every { webClient.post().uri(properties.endpoint) } returns requestSpec
        every { requestSpec.bodyValue(capture(bodySlot)) } returns requestSpec
        every { requestSpec.retrieve().bodyToMono(String::class.java).block() } returns "OK"

        val doi = testInstance.calculateDoi(TEST_ACC_NO, submitRequest)
        val body = bodySlot.captured
        val requestFile = body[FILE_PARAM]!!.first() as FileSystemResource
        val expectedXml = Files.readString(Paths.get("src/test/resources/ExpectedDOIRequest.xml"))

        assertThat(doi).isEqualTo("$BS_DOI_ID/$TEST_ACC_NO")
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
    fun `doi not requested`() {
        val submission = submission {
            title = "Test Submission"

            section("Study") {
                attribute("Type", "Experiment")
            }
        }

        every { submitRequest.submission } returns submission

        assertThat(testInstance.calculateDoi(TEST_ACC_NO, submitRequest)).isNull()
        verify(exactly = 0) { webClient.post() }
    }

    @Test
    fun `already existing DOI`() {
        val previousVersionDoi = "$BS_DOI_ID/$TEST_ACC_NO"
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "$BS_DOI_ID/$TEST_ACC_NO")

            section("Study") {
                attribute("Type", "Experiment")
            }
        }

        every { submitRequest.submission } returns submission
        every { previousVersion.doi } returns previousVersionDoi
        every { submitRequest.previousVersion } returns previousVersion

        val doi = testInstance.calculateDoi(TEST_ACC_NO, submitRequest)

        assertThat(doi).isEqualTo(previousVersionDoi)
        verify(exactly = 0) { webClient.post() }
    }

    @Test
    fun `invalid given DOI`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "10.287.71/$TEST_ACC_NO")

            section("Study") {
                attribute("Type", "Experiment")
            }
        }

        every { submitRequest.submission } returns submission
        every { previousVersion.doi } returns "$BS_DOI_ID/$TEST_ACC_NO"
        every { submitRequest.previousVersion } returns previousVersion

        val exception = assertThrows<InvalidDoiException> { testInstance.calculateDoi(TEST_ACC_NO, submitRequest) }

        assertThat(exception.message).isEqualTo("The given DOI should match the previous version")
        verify(exactly = 0) { webClient.post() }
    }

    @Test
    fun `missing title`() {
        val submission = submission {
            attribute("DOI", "")
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<MissingTitleException> { testInstance.calculateDoi(TEST_ACC_NO, submitRequest) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("A title is required for DOI registration")
    }

    @Test
    fun `missing organization`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Author") {
                    attribute("Name", "John Doe")
                }
            }
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<MissingDoiFieldException> { testInstance.calculateDoi(TEST_ACC_NO, submitRequest) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("The required DOI field 'organization' could not be found")
    }

    @Test
    fun `missing organization accNo`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Organization") {
                    attribute("Name", "EMBL")
                }
            }
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<InvalidOrgException> { testInstance.calculateDoi(TEST_ACC_NO, submitRequest) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("Organizations are required to have an accession")
    }

    @Test
    fun `missing organization name`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Organization") {
                    accNo = "o1"
                    attribute("Institue", "American Society")
                }

                section("Organization") {
                    accNo = "o2"
                    attribute("Name", "EMBL")
                }

                section("Organization") {
                    accNo = "o3"
                    attribute("Research Associate", "Astrazeneca")
                }
            }
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<InvalidOrgNamesException> { testInstance.calculateDoi(TEST_ACC_NO, submitRequest) }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("The following organization names are empty: o1, o3")
    }

    @Test
    fun `missing author name`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Organization") {
                    accNo = "o1"
                    attribute("Name", "EMBL")
                }

                section("Author") {
                    attribute("P.I.", "John Doe")
                    attribute("ORCID", "12-32-45-82")
                    attribute("Affiliation", "o1", ref = true)
                }
            }
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<InvalidAuthorNameException> {
            testInstance.calculateDoi(TEST_ACC_NO, submitRequest)
        }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("Authors are required to have a name")
    }

    @Test
    fun `missing affiliation`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Organization") {
                    accNo = "o1"
                    attribute("Name", "EMBL")
                }

                section("Author") {
                    attribute("Name", "John Doe")
                    attribute("ORCID", "12-32-45-82")
                }
            }
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<MissingAuthorAffiliationException> {
            testInstance.calculateDoi(TEST_ACC_NO, submitRequest)
        }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message).isEqualTo("Authors are required to have an affiliation")
    }

    @Test
    fun `invalid affiliation`() {
        val submission = submission {
            title = "Test Submission"
            attribute("DOI", "")

            section("Study") {
                section("Organization") {
                    accNo = "o1"
                    attribute("Name", "EMBL")
                }

                section("Author") {
                    attribute("Name", "John Doe")
                    attribute("ORCID", "12-32-45-82")
                    attribute("Affiliation", "o2", ref = true)
                }
            }
        }

        every { submitRequest.submission } returns submission

        val exception = assertThrows<InvalidAuthorAffiliationException> {
            testInstance.calculateDoi(TEST_ACC_NO, submitRequest)
        }

        verify(exactly = 0) { webClient.post() }
        assertThat(exception.message)
            .isEqualTo("The organization 'o2' affiliated to the author 'John Doe' could not be found")
    }

    companion object {
        private const val TEST_ACC_NO = "S-TEST123"

        private val properties = DoiProperties(
            endpoint = "https://test-endpoint.org",
            uiUrl = "https://www.biostudies.ac.uk",
            user = "a-user",
            password = "a-password",
        )
    }
}
